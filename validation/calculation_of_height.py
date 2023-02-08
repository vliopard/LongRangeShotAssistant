import math

angle = 3
distance = 10

def get_height(angle, distance):
    tan = math.tan(angle * math.pi / 180)
    height = tan * distance
    return height
    

print(get_height(3, 30))