import math
import tkinter

# constants
g = 9.81    # N/kg
m = 0.009   # kg
d = 0.0053  # m
C = 9 * 0.3
r = 1.2   # kg/m^3

# from mythbusters
# starting height = 300 feet = 91.44 m
# final speed = 105 mph = 46.9 m/s

A = math.pi * (d / 2) ** 2

vt = math.sqrt(2 * m * g / (r * A * C))
print(f'Terminal Velocity [{vt:,.4f}]')

y = 1.7      # m - starting height
y2 = y       # other object starts the same height
vff = 46.94  # this is 105 mph from the show
v = 48       # m/s - just picked a starting velocity
v2 = v
t = 0
dt = 0.01

while y >= 0:
    F = -m * g + .5 * r * A * C * v ** 2
    v = v + F * dt / m
    v2 = v2 - g * dt
    y = y + v * dt
    y2 = y2 + v2 * dt
    t = t + dt
    #f1.plot(t, y)
    #f2.plot(t, y2)

print(f'Time [{t:,.4f}]')
print(f'Final Speed [{-v:,.4f}]')
print(f'No Air Resistance Speed [{-v2:,.4f}]')
