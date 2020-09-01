import csv
import sys
import numpy as np
import matplotlib.pyplot as plt

available_colors = ["black", "yellow", "magenta", "cyan", "red", "green", "blue"]

points = []
periods = []
labels = []
super_labels = []

super_colors = {}

with open(sys.argv[1]) as csv_file:
    reader = csv.reader(csv_file, delimiter=',')
    for row in reader:
        points.append([float(row[0]), float(row[1])])
        periods.append(int(row[2]))
        labels.append(int(row[3]))
        sl = int(row[4])
        super_labels.append(sl)
        if not sl in super_colors:
            super_colors[sl] = available_colors.pop()

points = np.array(points)
periods = np.array(periods)
labels = np.array(labels)
super_labels = np.array(super_labels)
colors = [ super_colors[l] for l in super_labels ]

fig, ax = plt.subplots()
ax.scatter(points[:,0], points[:,1], c=colors)

for i, txt in enumerate(periods):
    s = str(txt) + "," + str(i)
    if i == labels[i]:
        s = s + ",M" 
    ax.annotate(s, (points[i,0] +1, points[i,1] +1))

plt.show()