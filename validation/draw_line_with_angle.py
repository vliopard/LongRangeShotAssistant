import math
import tkinter

canvas_width = 1800
canvas_height = 300


root = tkinter.Tk()
canvas = tkinter.Canvas(root)
canvas.config(width=canvas_width, height=canvas_height)
canvas.pack()


def draw_line_with_angle(angle, center_x, center_y, line_length):
    angle_in_radians = angle * math.pi / 180
    end_x = center_x + line_length * math.cos(angle_in_radians)
    end_y = center_y + line_length * math.sin(angle_in_radians)
    canvas.create_line(center_x, center_y, end_x, end_y, fill='Red', width=1)


root.mainloop()