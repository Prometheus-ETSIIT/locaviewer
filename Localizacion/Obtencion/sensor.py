#Se le indica la MAC del bluetooth
#Saca el RSSI y lo envia

from BluezInquiry import *
import sys

#MAC del sensor
MAC_blue=sys.argv[1]

#Averiguamos su ID
ID_blue=bluez.hci_get_route(MAC_blue)

DUTYCICLES = 2

def inquiry(inquirier):
    inquirier.inquiry()
    while inquirier.is_inquiring():
        inquirier.process_event()
    
    return inquirier.get_mean()

def inquiry_times(inquirier, times):
    inquirier.clear_samples()
    for i in range(times):
        inquiry(inquirier)
    return inquirier.get_mean()


    
inquirier = BluezInquiry(ID_blue)



while True:    
    RSSI = inquiry_times(inquirier, DUTYCICLES)
    #envio
    
            

    

    
