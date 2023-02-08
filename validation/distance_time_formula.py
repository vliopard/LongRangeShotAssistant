import math
from scipy.special import lambertw

g = 9.81
teta = 45
v0 = 48
m = 9
b = 3.41557

y = b / m
A = ((y * v0 * math.sin(math.radians(teta))) / g) + 1
tempo_de_voo = A + lambertw(-A * math.exp(-A))
distancia_percorrida = v0 * math.cos(math.radians(teta)) * (tempo_de_voo / (y * A))

print(f'Time of Flight [{tempo_de_voo.real:,.5f}], Range Distance [{distancia_percorrida.real:,.5f}]')

