import math
import tkinter

canvas_width = 1800
canvas_height = 300

root = tkinter.Tk()
canvas = tkinter.Canvas(root)
canvas.config(width=canvas_width, height=canvas_height)
canvas.pack()

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


def shoot(speed, angle, position_x, position_y):
    raio = 0.65 / 2
    mass = 9
    time = 0
    gravity = 9.81
    time_interval = 0.001
    density = 1.2
    friction = density * math.pow(raio, 2)

    speed_x = speed * math.cos(math.radians(angle))
    speed_y = speed * math.sin(math.radians(angle))

    first = True
    first_distance = 0

    # constants
    gravitational_constant = 9.81  # N/kg
    mass_of_arrow = 0.009  # kg
    diameter_of_arrow = 0.0065  # m
    drag_coefficient = 9 * 0.3
    density_of_air = 1.2  # kg/m^3

    # from mythbusters
    # starting height = 300 feet = 91.44 m
    # final speed = 105 mph = 46.9 m/s

    cross_sectional_area = math.pi * (diameter_of_arrow / 2) ** 2

    terminal_velocity = math.sqrt(2 * mass_of_arrow * gravitational_constant / (density_of_air * cross_sectional_area * drag_coefficient))
    print(f'Terminal Velocity [{terminal_velocity:,.4f}]')

    starting_arrow_height = 1.7  # m - starting height
    starting_arrow_height_2 = starting_arrow_height  # other object starts the same height
    starting_velocity = 48  # m/s - just picked a starting velocity
    starting_velocity_2 = starting_velocity
    starting_time = 0
    time_step = 0.001

    while position_y >= 0:
        vx = speed_x
        px = position_x
        py = position_y

        accel_x = -friction * speed_x / mass
        accel_y = -friction * speed_y / mass - gravity

        position_x = position_x + speed_x * time_interval
        position_y = position_y + speed_y * time_interval

        total_force = -mass_of_arrow * gravitational_constant + 0.5 * density_of_air * cross_sectional_area * drag_coefficient * starting_velocity ** 2

        acceleration = total_force / mass_of_arrow

        #starting_velocity = starting_velocity + acceleration * time_step
        starting_velocity = starting_velocity + total_force * time_step / mass_of_arrow

        starting_velocity_2 = starting_velocity_2 - gravitational_constant * time_step

        starting_arrow_height = starting_arrow_height + starting_velocity * time_step

        starting_arrow_height_2 = starting_arrow_height_2 + starting_velocity_2 * time_step
        starting_time = starting_time + time_step

        draw_element(position_x, starting_arrow_height, 'green', 1, 1)
        draw_element(position_x, starting_arrow_height_2, 'gray', 1, 1)

        speed_x = speed_x + accel_x * time_interval
        speed_y = speed_y + accel_y * time_interval

        time = time + time_interval

        draw_element(position_x, position_y, 'red', 1, 1)

        dist = math.floor(position_x)
        if first:
            print(f'TIME [{time:,.2f}] SPEED: [{vx:,.2f}] DISTANCE: [{px:,.2f}] HEIGHT: [{py:,.2f}]')
            first = False

        if dist != first_distance:
            canvas.create_text((dist * global_scale) - 10, 290, text=dist, fill="black", font='Helvetica 9')
            draw_wall(dist * global_scale, 'green', 55, 1)
            first_distance = dist
            print(f'TIME [{time:,.2f}] SPEED: [{vx:,.2f}] DISTANCE: [{px:,.2f}] HEIGHT: [{py:,.2f}]')

    print(f'Time [{starting_time:,.4f}]')
    print(f'Final Speed [{-starting_velocity:,.4f}]')
    print(f'No Air Resistance Speed [{-starting_velocity_2:,.4f}]')


initial_speed = 48
inclination_angle = 0

initial_position = 0
initial_height = 1.6

draw_element(initial_position, initial_height, 'blue', 4, 4)

shoot(initial_speed, inclination_angle, initial_position, initial_height)
canvas.create_line(0, invert(0), canvas_width, invert(0), fill='black', width=3)
canvas.create_line(0, invert(initial_height * global_scale), canvas_width, invert(initial_height * global_scale), fill='blue', width=1)

root.mainloop()
