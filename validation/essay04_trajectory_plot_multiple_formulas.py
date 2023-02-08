import math
import tkinter

canvas_width = 1800
canvas_height = 300

root = tkinter.Tk()
canvas = tkinter.Canvas(root)
canvas.config(width=canvas_width, height=canvas_height)
canvas.pack()

# global_scale = 23
global_scale = 23


def invert(val):
    return canvas_height - val


def draw_wall(x_value, color, height, size):
    global global_scale
    canvas.create_line(x_value, invert(height), x_value, invert(0), fill=color, width=size)


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


def shoot(starting_velocity, angle, starting_arrow_x0, starting_arrow_y_red):
    diameter_of_arrow_meters = 0.0065
    radius_of_arrow = diameter_of_arrow_meters / 2
    mass_of_arrow_kilos = 0.009
    starting_time = 0
    gravitational_constant = 9.81
    time_step = 0.01
    density_of_air_kg_m3 = 1.2
    # stp = starting_arrow_y_red
    stp = 0
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

    while starting_arrow_y_red >= stp or starting_arrow_y_green >= stp or starting_arrow_y_gray >= stp:
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
        draw_element(starting_arrow_x0, starting_arrow_y_green, 'green', 1, 1)
        draw_element(starting_arrow_x0, starting_arrow_y_gray, 'gray', 1, 1)

        dist = math.floor(starting_arrow_x0)
        if first:
            print(f'TIME [{starting_time:,.2f}] SPEED: [{vx:,.2f}] DISTANCE: [{px:,.2f}] HEIGHT: R[{py_red:,.2f}] G[{py_green:,.2f}] A[{py_gray:,.2f}]')
            first = False

        if dist != first_distance:
            canvas.create_text((dist * global_scale) - 7, 290, text=dist, fill="black", font='Helvetica 7')
            draw_wall(dist * global_scale, 'green', 55, 1)
            first_distance = dist
            print(f'TIME [{starting_time:,.2f}] SPEED: [{vx:,.2f}] DISTANCE: [{px:,.2f}] HEIGHT: R[{py_red:,.2f}] G[{py_green:,.2f}] A[{py_gray:,.2f}]')

    print(f'TIME [{starting_time:,.2f}] SPEED: [{-starting_velocity_y_green:,.2f}]')
    print(f'No Air Resistance Speed [{-starting_velocity_y_gray:,.2f}]')


initial_speed = 48
inclination_angle = 5
initial_x = 0
initial_y = 1.6

draw_element(initial_x, initial_y, 'blue', 4, 4)

shoot(initial_speed, inclination_angle, initial_x, initial_y)
canvas.create_line(0, invert(0), canvas_width, invert(0), fill='black', width=3)
canvas.create_line(0, invert(initial_y * global_scale), canvas_width, invert(initial_y * global_scale), fill='blue', width=1)

root.mainloop()
