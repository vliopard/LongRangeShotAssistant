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

def reticle(top, left, size):
    right = top + size
    bottom = left + size
    
    canvas.create_line(right / 2 + left / 2, left, right / 2 + left / 2, right, fill='black', width=1)
    canvas.create_line(left, bottom / 2 + top / 2, right, bottom / 2 + top / 2, fill='black', width=1)
    canvas.create_oval(left, top, right, bottom, width=3)

reticle(100, 100, 200)

root.mainloop()
