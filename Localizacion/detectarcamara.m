function [m ,posicion] = detectarcamara(rssi,posicion_bluetooth,posicion_camara2,ancho_habitacion,alto_habitacion,angulo_pos)
%Argumentos
%------------------------------------------------------
%rssi=[-89,-85,-85]; %Potencia rssi(i) leida por el sensor i  en dBm
%posicion_bluetooth=[0,0;6,0;0,6]; %Posicion del bluetooth: Filas-> bluetooth. Columna-> X,Y
%posicion_camara2=[3,0;3,6;0,3;6,3];% el tercer elemento de cada fila indican si estan en la pared vertical u horizontal
%ancho_habitacion=6; 
%alto_habitacion=6;
%angulo_pos=[pi/2,pi/2,pi/2,pi/2] %angulo de la posicion de la camara. pi/2
%radianes seria cuando la camara está perpendicular a la pared. 0 radianes cuando la camara esta girada totalmente hacia la izquierda y pi rad cuando esta totalmente girada a la derecha 
%-------------------------------------------------------

%Devuelve
%-------------------------------------------------------
%posicion-La posicion de la camara graba
%m-Posicion del niño
%-------------------------------------------------------
%calculo de la pared en la que esta cada camara
posicion_camara=zeros(length(posicion_camara2(:,1)),3);
posicion_camara(:,1:2)=posicion_camara2;
for i=1:length(posicion_camara2(:,1))
    if posicion_camara2(i,1)==0 || posicion_camara2(i,1)==ancho_habitacion
        posicion_camara(i,3)=1;
    else
        posicion_camara(i,3)=0;
    end
end

%Calculamos la distancia 
error=3; %error en la detccion de la distancia a los sensores
r = -0.00680102923817849*rssi.^3 - 1.04905123190747*rssi.^2 - 59.2087843354658*rssi - 1106.35595941215; %Conversion rssi-distancia
r=r./100; %para pasarlo a metros (la formula lo da en cm)

%Triangulacion
x=[posicion_bluetooth(:,1)'];
y=[posicion_bluetooth(:,2)'];

N=length(x); %Numero de sensores bluetooth
hx=[0];
hy=[0];
n=0;
contador=0;
for i=1:length(rssi)
    if r(i)>=0 && r(i)<=sqrt(ancho_habitacion^2+alto_habitacion^2)+error
        contador=contador+1;
    end
end
if contador==length(rssi)
    for j=1:N-1
        for i=1+j:N
            n=n+1;

            hx(n)=2.*(x(j)-x(i));
            hy(n)=2.*(y(j)-y(i));
            C(n)=r(i).^2-r(j).^2+x(j).^2-x(i).^2+y(j).^2-y(i).^2;
        end
    end
    C=C';
    H=[hx',hy'];

    m=(inv(H'*H)*H')*C; %posicion x e y del objeto


    %Deteccion de la mejor camara para observar al objetivo:

    numero_camaras=size(posicion_camara,1);
    posicion_chavea=m;
    if 0<posicion_chavea(1) && posicion_chavea(1)<ancho_habitacion
        if 0<posicion_chavea(2) && posicion_chavea(2)<alto_habitacion
            angulo_vision=42*pi/180;%en grados
            %camaras eje x

            encima_recta_inferior=0;
            debajo_recta_superior=0;
            for i=1:numero_camaras
                dentro_rango_vision=0;


                encima_recta_inferior=0;
                debajo_recta_superior=0;
                if posicion_camara(i,3)==0
                    %recta inferior
                    punto_2_recta_inferior(i)=posicion_camara(i,1)-tan(angulo_vision/2-(pi/2-angulo_pos(i)))*abs(posicion_chavea(2)-posicion_camara(i,2));
                  
                    if posicion_chavea(1)>=punto_2_recta_inferior(i)
                        encima_recta_inferior=1;
                    end
                    %recta superior
                    punto_2_recta_superior(i)=posicion_camara(i,1)+tan(angulo_vision/2+(pi/2-angulo_pos(i)))*abs(posicion_chavea(2)-posicion_camara(i,2));
                    if posicion_chavea(1)<=punto_2_recta_superior(i)
                        debajo_recta_superior=1;
                    end
                else
                    %recta inferior
                    punto_2_recta_inferior(i)=posicion_camara(i,2)-tan(angulo_vision/2+(pi/2-angulo_pos(i)))*abs(posicion_chavea(1)-posicion_camara(i,1));
                    if posicion_chavea(2)>=punto_2_recta_inferior(i)
                        encima_recta_inferior=1;
                    end
                    %recta superior
                    punto_2_recta_superior(i)=posicion_camara(i,2)+tan(angulo_vision/2-(pi/2-angulo_pos(i)))*abs(posicion_chavea(1)-posicion_camara(i,1));                    
                    if posicion_chavea(2)<=punto_2_recta_superior(i)
                        debajo_recta_superior=1;
                    end
                end
               
                if debajo_recta_superior==1 && encima_recta_inferior==1
                    dentro_rango_vision=1;
                end
                %distancia a camaras
                if dentro_rango_vision==1
                    distancia(i)=sqrt((posicion_camara(i,1)-posicion_chavea(1))^2+(posicion_camara(i,2)-posicion_chavea(2))^2);
                else
                    distancia(i)=-1;
                end
            end

            %
            posicion=-1;
            nosalir=1;
            i=1;
            while nosalir 
                if distancia(i)>=0
                    minimo=distancia(i);
                    nosalir=0;
                elseif i==numero_camaras
                    nosalir=0;
                    minimo=-1;
                end
                i=i+1;

            end
            for i=1:numero_camaras
                if distancia(i)>=0
                    if distancia(i)<=minimo
                        minimo=distancia(i);
                        posicion=i;
                    end
                end
            end
        else
            posicion=-1;
        end
    else
            posicion=-1;
    end
else
    posicion=-1;
end
