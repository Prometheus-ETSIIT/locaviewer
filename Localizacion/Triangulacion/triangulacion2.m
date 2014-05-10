clear all
close all

x=[0,3,0] %x(i) posicion X del sensor bluetooth i
y=[0,0,3] %y(i) posicion Y del sensor bluetooth i
r=[2,2,2] %r(i) distacia calculada del sensor bluetooth i
N=length(x) %Numero de sensores bluetooth
hx=[0]
hy=[0]

n=0 

for j=1:N-1
    for i=1+j:N
        n=n+1

        hx(n)=2.*(x(j)-x(i))
        hy(n)=2.*(y(j)-y(i))
        C(n)=r(i).^2-r(j).^2+x(j).^2-x(i).^2+y(j).^2-y(i).^2
    end
end
C=C'
H=[hx',hy'];

m=(inv(H'*H)*H')*C %posicion x e y del objeto

%Pintando figuras
figure(1),
hold on
stem(x,y,'-b')
hold on
stem(m(1),m(2),'-r','s')
hold off