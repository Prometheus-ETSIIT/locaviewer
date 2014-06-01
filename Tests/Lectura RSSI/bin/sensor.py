#Se le indica la MAC del bluetooth
#Saca el RSSI y lo envia

from BluezInquiry import *
import sys

#Obtenemos la ID y el puerto por el que se enviaran los datos
ID_blue=int(sys.argv[1])
Port=int(sys.argv[2])


def inquiry(inquirier):
    inquirier.inquiry()
    while inquirier.is_inquiring():
        inquirier.process_event()
    
    return 


    
inquirier = BluezInquiry(ID_blue,Port)
inquiry(inquirier)

    
            

    

    
