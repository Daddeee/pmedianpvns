### Params and sets ###

# number of customers
param n;
# number of medians
param p;

# set of customers
set V := 0..n-1;

# distance between each pair of customers
param d {V,V} >= 0;

# average number of customers for each territory
param x_avg := n / p;

param alpha := 0.2 * (1/(n*n)) * (sum{i in V, j in V} d[i,j]); # avg dist

### Variables ###

var x {V,V} binary;

var y {V} >= 0;

### Objective ###

minimize distance_and_displacement:
        sum {i in V, j in V} d[i,j] * x[i,j]
        + alpha * sum {i in V} ( sum {j in V} x[i,j] - x_avg )^2;

### Constraints ###

subject to exclusive_assignment {j in V}:
        sum {i in V} x[i,j] = 1;

subject to number_of_medians:
        sum {i in V} x[i,i] = p;

subject to assign_to_medians_only {i in V, j in V}:
        x[i,j] <= x[i,i];