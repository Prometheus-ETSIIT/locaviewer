clear all
close all
numero_camaras_x=2;
numero_camaras_y=2;
posicion_camara_x=[3,0;3,6];
posicion_camara_y=[0,3;6,3];
ancho_habitacion=[0,6];
alto_habitacion=[0,6];
posicion_chavea=[5,-5];
if ancho_habitacion(1)<posicion_chavea(1) && posicion_chavea(1)<ancho_habitacion(2)
    if alto_habitacion(1)<posicion_chavea(2) && posicion_chavea(2)<alto_habitacion(2)
        angulo_vision=42*pi/180;%en grados
        %camaras eje x
        encima_recta_inferior=0;
        debajo_recta_superior=0;
        for i=1:numero_camaras_x
            dentro_rango_vision=0;
            %recta inferior
            encima_recta_inferior=0;
            punto_2_recta_inferior(i)=posicion_camara_x(i,1)-tan(angulo_vision/2)*abs(posicion_chavea(2)-posicion_camara_x(i,2));
            if posicion_chavea(1)>=punto_2_recta_inferior(i)
                encima_recta_inferior=1;
            end
            %recta superior
            debajo_recta_superior=0;
            punto_2_recta_superior(i)=posicion_camara_x(i,1)+tan(angulo_vision/2)*abs(posicion_chavea(2)-posicion_camara_x(i,2))
            if posicion_chavea(1)<=punto_2_recta_superior(i)
                debajo_recta_superior=1;
            end
            if debajo_recta_superior==1 && encima_recta_inferior==1
                dentro_rango_vision=1;
            end
            %distancia a camaras
            if dentro_rango_vision==1
                distancia(i)=sqrt((posicion_camara_x(i,1)-posicion_chavea(1))^2+(posicion_camara_x(i,2)-posicion_chavea(2))^2)
            else
                distancia(i)=-1;
            end
        end
        %camaras eje y
        encima_recta_inferior=0;
        debajo_recta_superior=0;
        for j=i+1:numero_camaras_y+i
            dentro_rango_vision=0;
            %recta inferior
            encima_recta_inferior=0;
            punto_2_recta_inferiory(j-i)=posicion_camara_y(j-i,2)-tan(angulo_vision/2)*abs(posicion_chavea(1)-posicion_camara_y(j-i,1));
            if posicion_chavea(2)>=punto_2_recta_inferiory(j-i)
                encima_recta_inferior=1;
            end
            %recta superior
            debajo_recta_superior=0;
            punto_2_recta_superiory(j-i)=posicion_camara_y(j-i,2)+tan(angulo_vision/2)*abs(posicion_chavea(1)-posicion_camara_y(j-i,1))
            if posicion_chavea(2)<=punto_2_recta_superiory(j-i)
                debajo_recta_superior=1;
            end
            if debajo_recta_superior==1 && encima_recta_inferior==1
                dentro_rango_vision=1;
            end
            %distancia a camaras
            if dentro_rango_vision==1
                distancia(j)=sqrt((posicion_camara_y(j-i,1)-posicion_chavea(1))^2+(posicion_camara_y(j-i,2)-posicion_chavea(2))^2)
            else
                distancia(j)=-1;
            end
        end
        figure(1),
        hold on
        stem(posicion_camara_x(:,1),posicion_camara_x(:,2),'-r')
        hold on
        stem(posicion_camara_y(:,1),posicion_camara_y(:,2),'-r')
        hold on
        stem(posicion_chavea(1),posicion_chavea(2),'-b')
        hold on
        vector=[posicion_chavea(1,1),posicion_chavea(1,1)]
        stem(vector,punto_2_recta_superiory,'-r')
        hold on
        stem(vector,punto_2_recta_inferiory,'-m')
        hold on
        vector=[posicion_chavea(2),posicion_chavea(2)]
        stem(punto_2_recta_superior,vector,'-y')
        hold on
        stem(punto_2_recta_inferior,vector,'-g')
        hold off
        posicion=-1;
        minimo=distancia(1);
        for i=1:numero_camaras_x+numero_camaras_y
            if distancia(i)>=0
                if distancia(i)<minimo
                    minimo=distancia(i)
                    posicion=i;
                end
            end
        end
    else
        posicion=-1;
    end
end
