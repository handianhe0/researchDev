#include <iostream>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>

using namespace std;

int main()
{
    int fd;
    bool *flag = new bool(false);
    string s;
    if( (fd = open("./fifo1",O_WRONLY) ) < 0)
    {
        cerr << "open FIFO Failed" << endl;
        exit(1);
    }

    while(true)
    {
        cout << "start or stop?  ";
        cin >> s;
        if( !s.compare("start") )
        {
            *flag = true;
            if( write(fd,reinterpret_cast<const char *>(flag),sizeof(flag) ) <0 )
            {
                cerr << "write FIFO Failed" << endl;
                exit(1);
            }
            cout << "started " << endl << flush;
        }
        else if( !s.compare("exit") )
        {
            return 0;
        }
        else
        {
            *flag = false;
            if( write(fd,reinterpret_cast<const char *>(flag),sizeof(flag) ) <0 )
            {
                cerr << "write FIFO Failed" << endl;
                exit(1);
            }
        }
    }

    delete flag;
    close(fd);
    return 0;
}