GlowScript 2.6 VPython

#these lines are to setup the two graphs
gd=graph(xtitle="Time [s]", ytitle="Vertical Height [m]")
f1=gcurve(color=color.red)
f2=gcurve(color=color.blue)

#constants
g=9.8 #N/kg
m=.019 #kg
d=0.0053 #m
C=9*.3
rho=1.2 #kg/m^3

#from mythbusters
#starting height = 300 feet = 91.44 m
#final speed = 105 mph = 46.9 m/s

A=pi*(d/2)**2

vt=sqrt(2*m*g/(rho*A*C))
print("terminal velocity = ", vt)

y=0 #m - starting height
y2=y #other object starts the same height
vff=46.94 #this is 105 mph from the show
v=60 # m/s - just picked a starting velocity
v2=v
t=0
dt=0.01

while y>=0:
  F=-m*g+.5*rho*A*C*v**2
  v=v+F*dt/m
  v2=v2-g*dt
  y=y+v*dt
  y2=y2+v2*dt
  t=t+dt
  f1.plot(t,y)
  f2.plot(t,y2)
print("final speed = ",-v)
print("no air resistance speed = ",-v2)

