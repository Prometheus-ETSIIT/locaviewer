#!/bin/bash
# Configura la publicación de vídeo.

# Configura la publicación de vídeo
sudo cp locaviewer_cams.sh /etc/init.d/
sudo chmod +x /etc/init.d/locaviewer_cams.sh
sudo update-rc.d locaviewer_cams.sh defaults
