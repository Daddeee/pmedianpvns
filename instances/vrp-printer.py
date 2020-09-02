import csv
import sys
import numpy as np
import matplotlib.pyplot as plt

available_colors = ["yellow", "magenta", "cyan", "red", "green", "blue"]

routes = {}
label_colors = { 0: "black", -1: "black" }

with open(sys.argv[1]) as csv_file:
    reader = csv.reader(csv_file, delimiter=',')
    for row in reader:
        splitted = row[0].split(" ")
        if splitted[0] == "Route":
            code = splitted[1]
            routes[code] = []
        else:
            label = int(row[2])
            if not label in label_colors:
                label_colors[label] = available_colors.pop()
            routes[code].append([float(row[0]), float(row[1]), label])

fig, ax = plt.subplots()

for key in routes:
    route = np.array(routes[key])

    points = route[:,:2]
    labels = route[:,2]
    colors = [ label_colors[l] for l in labels ]

    ax.scatter(points[:,0], points[:,1], c=colors)
    ax.plot(points[:,0], points[:,1], 'k')

plt.show()