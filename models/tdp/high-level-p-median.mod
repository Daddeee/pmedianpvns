### Params and sets ###

# number of points
param n;

# number of medians
param p;

# number of periods
param m;

# set of points
set V := 0..n-1;

# set of periods
set T := 0..m-1;

# distance between each pair of customers
param c {V,V} >= 0;

# period of each point
param t {V,T} binary;

### Variables ###

var x {V,V} binary;

### Objective ###

minimize distance_and_displacement:
    sum {i in V, j in V} c[i,j] * x[i,j];

### Constraints ###

subject to exclusive_assignment {i in V}:
    sum {j in V} x[i,j] = 1;

subject to number_of_medians:
    sum {j in V} x[j,j] = p;

subject to number_of_assignments {j in V}:
    sum {i in V} x[i,j] = m*x[j,j];

subject to assign_to_medians_only {i in V, j in V}:
    x[i,j] <= x[j,j];

subject to assign_one_per_period {k in T, j in V}:
    sum {i in V} x[i,j] * t[i,k] = x[j,j];