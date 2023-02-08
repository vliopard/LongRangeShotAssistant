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

    while position_y >= 0:
        vx = speed_x
        px = position_x
        py = position_y

        accel_x = -friction * speed_x / mass
        accel_y = -friction * speed_y / mass - gravity

        position_x = position_x + speed_x * time_interval
        position_y = position_y + speed_y * time_interval

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


initial_speed = 48
inclination_angle = 0

initial_position = 0
initial_height = 1.6

draw_element(initial_position, initial_height, 'blue', 4, 4)

shoot(initial_speed, inclination_angle, initial_position, initial_height)
canvas.create_line(0, invert(0), canvas_width, invert(0), fill='black', width=3)
canvas.create_line(0, invert(initial_height*global_scale), canvas_width, invert(initial_height*global_scale), fill='blue', width=1)

root.mainloop()

