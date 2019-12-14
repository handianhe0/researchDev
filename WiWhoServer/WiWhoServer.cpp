#include <iostream>
#include <arpa/inet.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <strings.h>
#include <cstring>
#include <thread>
#include <fstream>
#include <string>

using namespace std;

const int port = 6800;
struct s_info
{
    struct sockaddr_in client_addr;
    int cli_fd;
} s[10];

// a thread to save and print the data 
void client_tackle(struct s_info arg,bool* r_flag)
{
    struct s_info *arg_cli = &arg;
    char buff[1024]={0},buff_s[1028]={0};
    bool prev_r_flag = false;
    char t[2] = {'0','1'};

    string filepath = "./";
    filepath.append(inet_ntoa(arg_cli->client_addr.sin_addr) ).append("Sensor.txt");
    ofstream fileSensor(filepath,ios::binary);
    fileSensor << inet_ntoa(arg_cli->client_addr.sin_addr) << " " << flush;

    // set arg_cli->cli_fd as nonblocked IO
    int block_flags;
    if( block_flags = fcntl(arg_cli->cli_fd,F_GETFL,0) <0 )
    {
        cerr << "fcntl error" << endl;
        exit(1);
    }
    block_flags |= O_NONBLOCK;
    if(fcntl(arg_cli->cli_fd,F_SETFL,block_flags) < 0 )
    {
        cerr << "fcntl error" << endl;
        exit(1);
    }

    while(true)
    {
        int read_num = read(arg_cli->cli_fd,buff,sizeof(buff) );
        if(-1 != read_num)
        {
            if(0 == read_num)
            {
                cout << inet_ntoa(arg_cli->client_addr.sin_addr) << ":" << ntohs(arg_cli->client_addr.sin_port)
                            << " socket stream closed" << endl << flush;
                break;
            }
            else
            {
                fileSensor << buff  << flush;
                cout << buff << flush ;//<< " " << flush;
                bzero(buff,sizeof(buff) );
            }
        }

        // send orders to client to control its on and off
        if( (*r_flag) != prev_r_flag )
        {
            prev_r_flag = *r_flag;
            if( *r_flag)
            {
                if(write(arg_cli->cli_fd,t+1,sizeof(char) ) < 0)
                {
                    cerr << "write error1 " << endl;
                }
                else
                {
                    cout << "write1 successfully " << endl;
                }
            }
            else
            {
                if(write(arg_cli->cli_fd,t,sizeof(char) ) < 0)
                {
                    cerr << "write error2 " << endl;
                }
                else
                {
                    cout << "write2 successfully " << endl;
                }
            }
        }
    }

    fileSensor.close();
    close(arg_cli->cli_fd);
    // system("pause");
    return ;
}

// a thread to listen to a fifo that control this server app
int control_TR(bool* re_flag)
{
    int fd;
    int len;
    if(mkfifo("fifo1",0666) < 0 && errno != EEXIST)
    {
        cerr << "Create FIFO Failed" << endl;
    }

    if( (fd = open("fifo1",O_RDONLY ) ) < 0)
    {
        cerr << "open FIFO Failed" <<endl;
    }

    while( (len = read(fd,re_flag,sizeof(re_flag) ) ) > 0)
    {
        cout << "read FIFO message:" << *re_flag << endl << flush;
    }

    close(fd);
    if( remove("./fifo1") < 0)
    {
        cerr << "remove error " << endl;
    }
    exit(0);
}

int main()
{
    // create a fifo and a valriable for control recieve or not
    bool* recev_flag = new bool(false);
    thread ctl(control_TR,recev_flag);
    ctl.detach();

    //  server and client address variable
    int sfd,n,cfd,i=0;
    struct sockaddr_in ser_addr,cli_addr;
    bzero(&cli_addr,sizeof(cli_addr) );

    //  set the server address
    ser_addr.sin_family = AF_INET;
    ser_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    ser_addr.sin_port = htons(port);
    socklen_t cli_addr_len;

    //  create a socket
    sfd = socket(AF_INET,SOCK_STREAM,0);
    setsockopt(sfd,SOL_SOCKET,SO_REUSEADDR,&n,4);

    // bind the IP address and port
    int ret = bind(sfd,reinterpret_cast<struct sockaddr *>(&ser_addr),sizeof(ser_addr) );
    if(ret<0)
    {
        cerr << "bind error";
    }

    // begin to listen
    listen(sfd,5);
    cout << endl << inet_ntoa(cli_addr.sin_addr) << ":" << ntohs(cli_addr.sin_port)  << " waiting for connect." << endl << flush;

    while(true)
    {
        //  accept the request
        cfd = accept(sfd,reinterpret_cast<struct sockaddr *>(&cli_addr),&cli_addr_len );
        s[i].cli_fd = cfd;
        s[i].client_addr = cli_addr;

        cout << inet_ntoa(cli_addr.sin_addr) << ":" << ntohs(cli_addr.sin_port) << " request to connect" << endl;

        // create a thread to save the data
        thread t(client_tackle,s[i],recev_flag );
        t.detach();

        i++;
        if(10 == i)
        {
            i = 0;
        }
    }
    
    close(sfd);
    delete recev_flag;
    return 0;
}
