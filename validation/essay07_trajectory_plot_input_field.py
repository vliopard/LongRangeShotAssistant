import math
import tkinter

canvas_width = 1800
canvas_height = 700

root = tkinter.Tk()
canvas = tkinter.Canvas(root)
canvas.config(width=canvas_width, height=canvas_height)
canvas.pack()

global_scale = 17
global_scale = 20
#global_scale = 23


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


def draw_line_with_angle(angle, center_x, center_y, line_length):
    angle_in_radians = angle * math.pi / 180
    end_x = center_x + line_length * math.cos(angle_in_radians)
    end_y = center_y + line_length * math.sin(angle_in_radians)
    canvas.create_line(center_x, invert(center_y), end_x, invert(end_y), fill='Red', width=1)


def convert_chart(y_value, x_value, color, size, m_value):
    x_value = canvas_height - x_value
    min_value = m_value
    max_value = min_value * 2
    canvas.create_line(y_value, x_value, y_value + max_value, x_value, fill=color, width=size)
    canvas.create_line(y_value + min_value, x_value - min_value, y_value + min_value, x_value + min_value, fill=color, width=size)


def shoot(starting_velocity, angle, starting_arrow_x0, starting_arrow_y_red, floor_lvl, floor_hgt):
    diameter_of_arrow_meters = 0.0065
    radius_of_arrow = diameter_of_arrow_meters / 2
    mass_of_arrow_kilos = 0.009
    starting_time = 0
    gravitational_constant = 9.81
    time_step = 0.01
    density_of_air_kg_m3 = 1.2
    # stp = starting_arrow_y_red
    stp = 60
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
    #while starting_arrow_x0 <= stp:
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

    print(f'TIME [{starting_time:,.2f}] SPEED: R[{-starting_velocity_y_red:,.2f}] G[{-starting_velocity_y_green:,.2f}] A[{-starting_velocity_y_gray:,.2f}]')
    #print(f'No Air Resistance Speed [{-starting_velocity_y_gray:,.2f}]')


aim_heigth = 1.6
floor_height = 10

floor_level = 0

initial_speed = 48
inclination_angle = 0
initial_x = floor_level
initial_y = floor_height + aim_heigth


def runme(inclination_angle):
    canvas.delete("all")
    draw_element(initial_x, initial_y, 'blue', 4, 4)
    shoot(initial_speed, inclination_angle, initial_x, initial_y, floor_level, floor_height)
    canvas.create_line(floor_level, invert(floor_height * global_scale), canvas_width, invert(floor_height * global_scale), fill='black', width=3)
    canvas.create_line(floor_level, invert(initial_y * global_scale), canvas_width, invert(initial_y * global_scale), fill='blue', width=1)
    draw_line_with_angle(inclination_angle, floor_level, initial_y * global_scale, 1500)

    codigo = tkinter.Entry(canvas)
    canvas.create_window(30,10,width=45,window=codigo)
    botao = tkinter.Button(canvas, text="Run", command=lambda: runme(float(codigo.get())))
    canvas.create_window(30,30,window=botao)
    canvas.update()

runme(0)

root.mainloop()
