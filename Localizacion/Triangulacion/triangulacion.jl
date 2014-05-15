# x[i] posicion X del sensor bluetooth i
# y[i] posicion Y del sensor bluetooth i
# r[i] distacia calculada del sensor bluetooth i

function pos = triangulacion(x, y, r)
  N = length(x); # Numero de sensores bluetooth

  D = -0.00680102923817849*r.^3 - 1.04905123190747*r.^2 - 59.2087843354658*r - 1106.35595941215;
  
  # Vectores con dos veces la diferencia entre una coordenada y el resto
  h_len = int((N - 1) * N / 2);
  hx    = zeros(1, h_len);    # Para el tamaño del vector, es igual a: 
  hy    = zeros(1, h_len);    # sumatoria de i = 0 a i = N - 1 de i
  C     = zeros(1, h_len);

  n = 0; 
  for j = 1 : N-1
    for i = 1+j : N
      
      n = n + 1;  # Índice del vector hx, hy
      hx[n] = 2 .* (x[j] - x[i]);
      hy[n] = 2 .* (y[j] - y[i]);
      
      C[n] = D[i].^2 - D[j].^2 + x[j].^2 - x[i].^2 + y[j].^2 - y[i].^2;
      
    end
  end

  C   = C';
  H   = [hx' hy'];
  pos = (inv(H' * H) * H') * C # Posición x e y del objeto

  # Pintando figuras
  # TODO: Esto en mi ordenador no va porque PyPlot para Julia
  # usa una versión más moderna de Qt que la que hay instalada por paquetes
  # y eso produce una excepción
  #
  #using PyPlot
  #figure(1);
  #hold on;
  #stem(x, y, '-b');
  #stem(m[1], m[2], '-r', 's');
  #hold off;
end