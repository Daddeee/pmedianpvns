### Params and sets ###

# number of customers
param n;
# number of medians
param p;

# set of customers
set V := 0..n-1;

# distance between each pair of customers
param d {V,V} >= 0;

param alpha := (1/(n*n))*(sum{i in V, j in V} d[i,j]);

### Variables ###

var x {V,V} binary;

var u >= 0;

var l >= 0;

### Objective ###

minimize most_uniform_unbalanceness:
    sum{i in V, j in V} d[i,j]*x[i,j]
    + alpha*(u - l)^2;

### Constraints ###

subject to exclusive_assignment {j in V}:
    sum {i in V} x[i,j] = 1;

subject to number_of_medians:
    sum {i in V} x[i,i] = p;

subject to assign_to_medians_only {i in V, j in V}:
    x[i,j] <= x[i,i];

subject to upper_bound {i in V}:
    u >= sum {j in V} x[i,j];

subject to lower_bound {i in V}:
    l <= sum {j in V} x[i,j] + n*(1 - x[i,i]);