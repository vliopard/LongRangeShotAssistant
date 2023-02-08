import math
import tkinter

square = 1000
canvas_width = square
canvas_height = square

root = tkinter.Tk()
canvas = tkinter.Canvas(root)
canvas.config(width=canvas_width, height=canvas_height)
canvas.pack()

global_scale = 1

padding = 10
left = padding
top = padding
right = canvas_width - padding
bottom = canvas_height - padding
canvas.create_line(right / 2 + left / 2, left, right / 2 + left / 2, right, fill='black', width=1)
canvas.create_line(left, bottom / 2 + top / 2, right, bottom / 2 + top / 2, fill='black', width=1)
canvas.create_oval(left, top, right, bottom, width=3)


def invert(val):
    return canvas_height - val


def draw_element(x_pos, y_pos, color, size, scale, lx):
    global global_scale
    scale_x = math.ceil(x_pos * global_scale)
    scale_y = math.ceil(y_pos * global_scale)
    convert_chart(scale_x, scale_y, color, size, scale, lx)


def convert_chart(y_value, x_value, color, size, m_value, lx):
    x_value = canvas_height - x_value

    height = -1
    length = m_value / 2
    to_left = 8
    canvas.create_line(y_value - length + to_left,
                       x_value - height,
                       y_value + length + to_left,
                       x_value - height,
                       fill=color,
                       width=size)
    lxt = f'{lx:,.2f}'
    canvas.create_text(canvas_width-30, canvas_height-30, text=lxt, fill="black", font='Helvetica 10')
    # canvas.create_line(y_value + min_value, x_value - min_value, y_value + min_value, x_value + min_value, fill=color, width=size)


def shoot(starting_velocity, angle, starting_arrow_x0, starting_arrow_y_red, stp):
    global left
    global right

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

    while starting_arrow_x0 <= stp:
        last_x = starting_arrow_x0
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

    center = (right / 2 - left / 2) + 2

    last_x = starting_arrow_x0
    draw_element(center, center + 8, 'blue', 1, 8, last_x)
    print(starting_arrow_y_red, starting_arrow_y_green, starting_arrow_y_gray)
    draw_element(center, invert((bottom / 2 + 20) - starting_arrow_y_red * 10), 'red', 3, 30, last_x)
    draw_element(center, invert((bottom / 2 + 20) - starting_arrow_y_green * 10), 'green', 3, 20, last_x)
    draw_element(center, invert((bottom / 2 + 20) - starting_arrow_y_gray * 10), 'black', 3, 10, last_x)


initial_speed = 48
inclination_angle = 0
distance = 25
initial_x = 0
initial_y = 1.6
shoot(initial_speed, inclination_angle, initial_x, initial_y, distance)

root.mainloop()
