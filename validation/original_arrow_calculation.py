#this just sets up a graph
gd=graph(xtitle="Time [s]", ytitle="Vertical Position [m]")
f1=gcurve(color=color.red)

#starting constants
g=9.8 #N/kg - gravitational constant

#change this
m=.019 #kg - mass of arrow

#change this
d=0.0053 #m - diameter of arrow

#change this
C=9*.3 #drag coefficient

rho=1.2 #kg/m^3 - density of air

#from mythbusters
#starting height = 300 feet = 91.44 m
#final speed = 105 mph = 46.9 m/s

#calculate the cross sectional area
A=pi*(d/2)**2

#calculate the terminal velocity (just for fun)
vt=sqrt(2*m*g/(rho*A*C))
print("terminal velocity = ", vt, " m/s")


#change this
y=91.44 #m - starting arrow height

#final velocity from the show
vff=46.94 #this is 105 mph from the show

v=0 #m - starting velocity
t=0 #s - starting time

#change this
dt=0.01 #s - time step

#loop of calculations.  Run until the arrow hits the ground
while y>0:
  
  #calculate the total force
  F=-m*g+.5*rho*A*C*v**2
  
  #calculate the acceleration
  a=F/m
  
  #calculate the velocity at the end of the time interval
  v=v+a*dt
  
  #calculate the position at end of interval
  y=y+v*dt
  
  #update time
  t=t+dt
  
  #this adds data to the graph
  f1.plot(t,y)

#this prints the final speed  
print("final speed = ",-v, " m/s")