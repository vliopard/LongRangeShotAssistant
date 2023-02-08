import math
import tkinter

canvas_width = 1600
canvas_height = 1000

root = tkinter.Tk()
canvas = tkinter.Canvas(root)
canvas.config(width=canvas_width, height=canvas_height)
canvas.pack()

global_scale = 17
global_scale = 20
# global_scale = 23


def get_height(angle, distance):
    tan = math.tan(angle * math.pi / 180)
    height = tan * distance
    return height


def reticle(left, top, size, angle, distance):
    right = left + size
    bottom = top + size
    
    height = get_height(angle, distance) * 85
    print(height)
    canvas.create_line(left, 
                       bottom / 2 + top / 2 - height,
                       right,
                       bottom / 2 + top / 2 - height,
                       fill='brown',
                       width=2)
                       
    canvas.create_line(right / 2 + left / 2,
                       top, 
                       right / 2 + left / 2, 
                       bottom, 
                       fill='black',
                       width=1)
                       
    canvas.create_line(left, 
                       bottom / 2 + top / 2,
                       right,
                       bottom / 2 + top / 2,
                       fill='black',
                       width=1)

    canvas.create_oval(left, top, right, bottom, width=3)


def reticle_aim(left, top, size, val, color, wid):
    global aim_heigth
    global floor_height
    right = left + size
    bottom = top + size
    
    dif = floor_height + aim_heigth
    cor = val - dif
    canvas.create_rectangle(left, top, right, bottom)
    centerx = left + (right / 2 - left / 2) - (wid / 2) + 1
    centery = (top / 2) + (bottom / 2) - math.ceil(cor * 85)
    print(f'FH[{floor_height}] AH[{aim_heigth}] V[{val}] D[{dif}] C[{cor}]')
    canvas.create_line(centerx, centery, centerx + wid, centery, fill=color, width=3)
    
 
def invert(val):
    return canvas_height - val


def draw_wall(x_value, color, height, size):
    global global_scale
    canvas.create_line(x_value, invert(height), x_value, invert(height - 300), fill=color, width=size)


def draw_element(x_pos, y_pos, color, size, scale):
    global global_scale
    scale_x = math.ceil(x_pos * global_scale)
    scale_y = math.ceil(y_pos * global_scale)
    convert_chart(scale_x, scale_y, color, size, scale)


def convert_chart(y_value, x_value, color, size, m_value):
    x_value = canvas_height - x_value
    min_value = m_value
    max_value = min_value * 2
    canvas.create_line(y_value, x_value, y_value + max_value, x_value, fill=color, width=size)
    canvas.create_line(y_value + min_value, x_value - min_value, y_value + min_value, x_value + min_value, fill=color, width=size)


def draw_line_with_angle(angle, center_x, center_y, line_length):
    angle_in_radians = angle * math.pi / 180
    end_x = center_x + line_length * math.cos(angle_in_radians)
    end_y = center_y + line_length * math.sin(angle_in_radians)
    canvas.create_line(center_x, invert(center_y), end_x, invert(end_y), fill='Red', width=1)


def shoot1(starting_velocity, angle, starting_arrow_x0, starting_arrow_y_red, floor_hgt, stop_me):
    diameter_of_arrow_meters = 0.0065
    radius_of_arrow = diameter_of_arrow_meters / 2
    mass_of_arrow_kilos = 0.009
    starting_time = 0
    gravitational_constant = 9.81
    time_step = 0.01
    density_of_air_kg_m3 = 1.2
    # stop_me = starting_arrow_y_red
    
    friction = density_of_air_kg_m3 * math.pow(radius_of_arrow, 2)

    starting_arrow_y_green = starting_arrow_y_red
    starting_arrow_y_gray = starting_arrow_y_red

    starting_velocity_x0 = starting_velocity * math.cos(math.radians(angle))
    starting_velocity_y_red = starting_velocity * math.sin(math.radians(angle))
    starting_velocity_y_green = starting_velocity * math.sin(math.radians(angle))
    starting_velocity_y_gray = starting_velocity * math.sin(math.radians(angle))

    first = True
    first_distance = 0

    drag_coefficient = 9 * 0.3
    cross_sectional_area = math.pi * (diameter_of_arrow_meters / 2) ** 2
    terminal_velocity = math.sqrt(2 * mass_of_arrow_kilos * gravitational_constant / (density_of_air_kg_m3 * cross_sectional_area * drag_coefficient))
    print(f'Initial  Velocity [{starting_velocity_x0:,.2f}]')
    print(f'Terminal Velocity [{terminal_velocity:,.2f}]')

    stop_running = True

    while stop_running:
        vx = starting_velocity_x0
        px = starting_arrow_x0
        py_red = starting_arrow_y_red
        py_green = starting_arrow_y_green
        py_gray = starting_arrow_y_gray

        total_force = -mass_of_arrow_kilos * gravitational_constant + 0.5 * density_of_air_kg_m3 * cross_sectional_area * drag_coefficient * starting_velocity_y_green ** 2

        acc = 1
        gc = 1

        acceleration = total_force / mass_of_arrow_kilos
        if acc:
            acceleration_x = acceleration
            acceleration_y = acceleration - gravitational_constant
        else:
            acceleration_x = -friction * starting_velocity_x0 / mass_of_arrow_kilos
            acceleration_y = -friction * starting_velocity_y_red / mass_of_arrow_kilos - gravitational_constant

        starting_arrow_x0 = starting_arrow_x0 + starting_velocity_x0 * time_step
        starting_arrow_y_red = starting_arrow_y_red + starting_velocity_y_red * time_step
        starting_arrow_y_green = starting_arrow_y_green + starting_velocity_y_green * time_step
        starting_arrow_y_gray = starting_arrow_y_gray + starting_velocity_y_gray * time_step

        if gc:
            starting_velocity_x0 = starting_velocity_x0 + acceleration_x * time_step
        else:
            starting_velocity_x0 = starting_velocity_x0 - gravitational_constant * time_step

        starting_velocity_y_red = starting_velocity_y_red + acceleration_y * time_step
        starting_velocity_y_gray = starting_velocity_y_gray - gravitational_constant * time_step
        starting_velocity_y_green = starting_velocity_y_green + acceleration * time_step

        starting_time = starting_time + time_step
        draw_element(starting_arrow_x0, starting_arrow_y_red, 'red', 1, 1)
        
        font_sz = 'Helvetica 7'
        ia = 90
        if py_red > 0:
            canvas.create_text(starting_arrow_x0 * global_scale, invert((floor_hgt + 4) * global_scale), text=f'{py_red - floor_hgt:,.2f}', fill="red", font=font_sz, angle=ia)
        
        draw_element(starting_arrow_x0, starting_arrow_y_green, 'green', 1, 1)
        if py_green > 0:
            canvas.create_text(starting_arrow_x0 * global_scale, invert((floor_hgt + 6) * global_scale), text=f'{py_green - floor_hgt:,.2f}', fill="green", font=font_sz, angle=ia)

        draw_element(starting_arrow_x0, starting_arrow_y_gray, 'gray', 1, 1)
        if py_gray > 0:
            canvas.create_text(starting_arrow_x0 * global_scale, invert((floor_hgt + 8) * global_scale), text=f'{py_gray - floor_hgt:,.2f}', fill="gray", font=font_sz, angle=ia)

        dist = math.floor(starting_arrow_x0)
        if first:
            print(f'TIME [{starting_time:,.2f}] SPEED: [{vx:,.2f}] DISTANCE: [{px:,.2f}] HEIGHT: R[{py_red:,.2f}] G[{py_green:,.2f}] A[{py_gray:,.2f}]')
            first = False

        if dist != first_distance:
            if not (py_red < 0 and py_green < 0 and py_gray < 0):
                canvas.create_text((dist * global_scale) - 7, invert(floor_hgt * global_scale) - 10, text=dist, fill="black", font='Helvetica 7')
                draw_wall(dist * global_scale, 'green', floor_hgt * global_scale + 70, 1)
                first_distance = dist
                print(f'TIME [{starting_time:,.2f}] SPEED: [{vx:,.2f}] DISTANCE: [{px:,.2f}] HEIGHT: R[{py_red:,.2f}] G[{py_green:,.2f}] A[{py_gray:,.2f}]')
            else:
                stop_running = False
                
        if stop_me and starting_arrow_x0 > stop_me:
            stop_running = False
            
    print(f'TIME [{starting_time:,.2f}] SPEED: R[{-starting_velocity_y_red:,.2f}] G[{-starting_velocity_y_green:,.2f}] A[{-starting_velocity_y_gray:,.2f}]')
    # print(f'No Air Resistance Speed [{-starting_velocity_y_gray:,.2f}]')
    

def shoot(starting_velocity, angle, starting_arrow_x0, starting_arrow_y_red, stop_me):
    diameter_of_arrow_meters = 0.0065
    radius_of_arrow = diameter_of_arrow_meters / 2
    mass_of_arrow_kilos = 0.009
    starting_time = 0
    gravitational_constant = 9.81
    time_step = 0.01
    density_of_air_kg_m3 = 1.2

    friction = density_of_air_kg_m3 * math.pow(radius_of_arrow, 2)

    starting_arrow_y_green = starting_arrow_y_red
    starting_arrow_y_gray = starting_arrow_y_red

    starting_velocity_x0 = starting_velocity * math.cos(math.radians(angle))
    starting_velocity_y_red = starting_velocity * math.sin(math.radians(angle))
    starting_velocity_y_green = starting_velocity * math.sin(math.radians(angle))
    starting_velocity_y_gray = starting_velocity * math.sin(math.radians(angle))

    drag_coefficient = 9 * 0.3
    cross_sectional_area = math.pi * (diameter_of_arrow_meters / 2) ** 2
    py_red = 0
    py_green = 0
    py_gray = 0

    while starting_arrow_x0 <= stop_me:
        py_red = starting_arrow_y_red
        py_green = starting_arrow_y_green
        py_gray = starting_arrow_y_gray

        total_force = -mass_of_arrow_kilos * gravitational_constant + 0.5 * density_of_air_kg_m3 * cross_sectional_area * drag_coefficient * starting_velocity_y_green ** 2

        acc = 1
        gc = 1

        acceleration = total_force / mass_of_arrow_kilos
        if acc:
            acceleration_x = acceleration
            acceleration_y = acceleration - gravitational_constant
        else:
            acceleration_x = -friction * starting_velocity_x0 / mass_of_arrow_kilos
            acceleration_y = -friction * starting_velocity_y_red / mass_of_arrow_kilos - gravitational_constant

        starting_arrow_x0 = starting_arrow_x0 + starting_velocity_x0 * time_step
        starting_arrow_y_red = starting_arrow_y_red + starting_velocity_y_red * time_step
        starting_arrow_y_green = starting_arrow_y_green + starting_velocity_y_green * time_step
        starting_arrow_y_gray = starting_arrow_y_gray + starting_velocity_y_gray * time_step

        if gc:
            starting_velocity_x0 = starting_velocity_x0 + acceleration_x * time_step
        else:
            starting_velocity_x0 = starting_velocity_x0 - gravitational_constant * time_step

        starting_velocity_y_red = starting_velocity_y_red + acceleration_y * time_step
        starting_velocity_y_gray = starting_velocity_y_gray - gravitational_constant * time_step
        starting_velocity_y_green = starting_velocity_y_green + acceleration * time_step

        starting_time = starting_time + time_step

    return py_red, py_green, py_gray


class Box:
    def __init__(self, left, top, right, bottom):
        self.left = left
        self.top = top
        self.right = right
        self.bottom = bottom


class RedDot:
    reticle_canvas = None
    padding_left = 0
    padding_top = 0
    box = Box(0, 0, 0, 0)

    reticle_size_px = 0
    lens_size_mt = 0
    lens_half_size_mt = 0

    eyes_height_mt = 0
    eyes_level_mt = 0
    object_distance_mt = 0

    angle_dg = 0
    center_y = 0

    def __init__(self, left, top, rd_canvas):
        self.padding_left = left
        self.padding_top = top
        self.box = Box(0, 0, 0, 0)
        self.reticle_canvas = rd_canvas

    def set_distance(self, distance_of_object):
        self.object_distance_mt = distance_of_object

    def set_angle(self, inclination_angle_value):
        self.angle_dg = inclination_angle_value

    def set_eyes_height(self, height):
        self.eyes_height_mt = height

    def scale_calc(self, meters_value):
        reticle_position = meters_value * self.reticle_size_px / 15
        return reticle_position

    def draw_box(self, left, top, right, bottom):
        self.box = Box(left, top, right, bottom)

        self.reticle_size_px = self.box.bottom - self.box.top

        self.reticle_canvas.create_rectangle(self.padding_left + left,
                                             self.padding_top + top,
                                             self.padding_left + right,
                                             self.padding_top + bottom)

    def draw_reticle(self):
        self.reticle_canvas.create_oval(self.box.left + self.padding_left,
                                        self.box.top + self.padding_top,
                                        self.box.right + self.padding_left,
                                        self.box.bottom + self.padding_top,
                                        width=3)

        self.reticle_canvas.create_line(self.box.right / 2 + self.box.left / 2 + self.padding_left,
                                        self.box.top + self.padding_top,
                                        self.box.right / 2 + self.box.left / 2 + self.padding_left,
                                        self.box.bottom + self.padding_top,
                                        fill='black',
                                        width=1)

        self.reticle_canvas.create_line(self.box.left + self.padding_left,
                                        self.box.bottom / 2 + self.box.top / 2 + self.padding_top,
                                        self.box.right + self.padding_left,
                                        self.box.bottom / 2 + self.box.top / 2 + self.padding_top,
                                        fill='black',
                                        width=1)

    def height(self):
        print(f'math.tan({self.angle_dg} * math.pi / 180) * {self.object_distance_mt} + {self.eyes_height_mt}')
        return math.tan(self.angle_dg * math.pi / 180) * self.object_distance_mt + self.eyes_height_mt

    def get_height(self, angle, dist):
        return math.tan(angle * math.pi / 180) * dist

    def get_center_x(self):
        return (self.box.right - self.box.left) / 2 + self.padding_left

    def convert_meters_in_pixels(self, meters, kind):
        if kind:
            diff = self.eyes_height_mt - self.lens_half_size_mt
        else:
            diff = self.eyes_level_mt - self.lens_half_size_mt
        return ((meters - diff) * self.reticle_size_px / self.lens_size_mt) + self.padding_top + 2

    def invert(self, sp):
        return self.box.bottom + self.padding_top - sp + 7

    def put_aim(self, meters, color):
        screen_pixels = self.invert(self.convert_meters_in_pixels(meters, True))
        canvas.create_line(self.get_center_x() - 4,
                           screen_pixels,
                           self.get_center_x() + 9,
                           screen_pixels,
                           fill=color,
                           width=3)

    def draw_level_line(self, line_color, line_width):
        self.eyes_level_mt = self.height()
        eyes_level_px = self.convert_meters_in_pixels(self.eyes_level_mt, True)
        print(f'LEVEL[{self.eyes_level_mt}]')
        self.center_y = (self.box.bottom - self.box.top) / 2 + eyes_level_px
        self.reticle_canvas.create_line(self.box.left + self.padding_left,
                                        eyes_level_px,
                                        self.box.right + self.padding_left + 2,
                                        eyes_level_px,
                                        fill=line_color,
                                        width=line_width)

    def reticle_aim(self, line_length, line_height, color):
        print(f'[{self.eyes_level_mt}][{self.eyes_height_mt}]')
        lh = self.convert_meters_in_pixels(line_height, False)
        center_y = self.invert(lh)

        center_x = self.get_center_x() - (line_length / 2) + 2

        canvas.create_line(center_x,
                           center_y,
                           center_x + line_length,
                           center_y,
                           fill=color,
                           width=3)

    def set_reticle_size(self, lens_range):
        self.lens_size_mt = lens_range
        self.lens_half_size_mt = lens_range / 2


aim_heigth = 1.6
floor_height = 10

floor_level = 0

initial_x = floor_level
initial_y = floor_height + aim_heigth

stopme = 18


def runme(inclination_angle, object_distance_mt):
    canvas.delete("all")
    initial_speed = 48

    draw_element(initial_x, initial_y, 'blue', 4, 4)
    shoot1(initial_speed, inclination_angle, initial_x, initial_y, floor_height, object_distance_mt)
    canvas.create_line(floor_level, invert(floor_height * global_scale), canvas_width, invert(floor_height * global_scale), fill='black', width=3)
    canvas.create_line(floor_level, invert(initial_y * global_scale), canvas_width, invert(initial_y * global_scale), fill='blue', width=1)
    draw_line_with_angle(inclination_angle, floor_level, initial_y * global_scale, 1500)

    rd = RedDot(900, 3, canvas)

    lens_height_range_in_pixels = 700
    rd.draw_box(2, 2, lens_height_range_in_pixels, lens_height_range_in_pixels)

    # sight_height_in_meters = 1.6 # 1.05
    eyes_height_mt = 1.6
    rd.set_eyes_height(eyes_height_mt)

    # lens_size_mt = 2.1
    lens_size_mt = 2.1
    rd.set_reticle_size(lens_size_mt)
    rd.draw_reticle()

    # inclination_angle = 3
    rd.set_angle(inclination_angle)

    # object_distance_mt = 3.7
    # object_distance_mt = 20
    rd.set_distance(object_distance_mt)

    # rd.put_aim(1.6, 'gray')
    # rd.put_aim(1.05, 'black')
    rd.draw_level_line('brown', 2)

    r, b, g = shoot(initial_speed, inclination_angle, 0, eyes_height_mt, object_distance_mt)
    rd.reticle_aim(23, r, 'red')
    rd.reticle_aim(19, b, 'blue')
    rd.reticle_aim(13, g, 'green')

    codigo = tkinter.Entry(canvas)
    canvas.create_window(30, 10, width=45, window=codigo)

    st = tkinter.Entry(canvas)
    canvas.create_window(100, 10, width=45, window=st)

    botao = tkinter.Button(canvas, text="Run", command=lambda: runme(float(codigo.get()), int(st.get())))
    canvas.create_window(30, 30, window=botao)
    canvas.update()


runme(0, 3.7)

root.mainloop()
