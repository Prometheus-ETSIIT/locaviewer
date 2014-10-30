#!/usr/bin/python
# -*- coding: utf-8 -*-
#
# Se le indica el puerto de envío y lo inicia para cada Bluetooth.
# Saca el RSSI y lo envia.

from BluezInquiry import BluezInquiry
import sys
from subprocess import Popen, PIPE
import threading

# Inquiry de forma infinita
def inquiry(inquirier):
    while True:
        inquirier.inquiry()
        while inquirier.is_inquiring():
            inquirier.process_event()


# Obtenemos la ID y el puerto por el que se enviaran los datos
port = int(sys.argv[1])

# Obtenemos la MAC del dispositivo a partir del ID
mac = None
hci_out = Popen(['hcitool', 'dev'], stdout=PIPE).stdout.readlines()
del hci_out[0]  # 'Devices:\n'

# Por cada dispositivo
for dev in hci_out:
    opts = dev[1:-1].split('\t')  # Elimino el primer tabulador y \n y divido
    if not opts[0][:3] == "hci":
        continue
    
    dev_id = opts[0][3:]
    mac = opts[1]
    print("[Bluetooth] Soy " + mac + " (" + dev_id + ")")

    # Inicia el inquiry para este Bluetooth
    inquirier = BluezInquiry(int(dev_id), mac, port)
    threading.Thread(target=inquiry, args=(inquirier, )).start()
