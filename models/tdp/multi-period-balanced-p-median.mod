###### Params ######

# number of customers
param n;

# number of medians
param p;

# number of periods
param m;

# set of customers
set N := 0..n-1;

# set of periods
set T := 0..m-1;

# travel cost between each pair of customers
param c {N,N} >= 0;

# release date of each customer
param r {N} integer >= 0;

# due date of each customer
param d {N} integer >= 0;

# average number of customers for each territory
param x_avg := n / (p*m);

# avg dist
param alpha;

# set of valid periods per customer
set V {i in N} := { t in T : d[i] >= t and t >= r[i] };

# set of non valid periods per customer
set I {i in N} := { t in T : t > d[i] or t < r[i] };

### Variables ###

var x {N,N,T} binary;

var y {N,N} binary;

var w {N} >= 0;

var z {N,N,T} binary;

### Objective ###

minimize distance_and_displacement:
    sum {t in T, i in N, j in N} x[i,j,t] * c[i,j] + sum {j in N} alpha * w[j] + sum {j in N, k in N} c[j,k] * y[j,k];

### Constraints ###

subject to exclusive_assignment_in_valid_period {i in N}:
    sum {j in N, t in V[i]} x[i,j,t] = 1;

subject to forbidden_assignent_outside_valid_periods {i in N}:
    sum {j in N, t in I[i]} x[i,j,t] = 0;

subject to number_of_medians_per_period {t in T}:
    sum {j in N} x[j,j,t] = p;

subject to assign_to_medians_only {i in N, j in N, t in T}:
    x[i,j,t] <= x[j,j,t];

subject to abs_1 {j in N}:
    sum {t in T} ( ( sum {i in N} x[i,j,t] ) - x[j,j,t] * x_avg ) <= w[j];

subject to abs_2 {j in N}:
    sum {t in T} ( x[j,j,t] * x_avg - ( sum {i in N} x[i,j,t] ) ) <= w[j];

subject to assign_to_supermedian_in_valid_periods {j in N}:
    sum {k in N} y[j,k] = sum {t in V[j]} x[j,j,t];

subject to number_of_supermedians:
    sum {k in N} y[k,k] = p;

subject to assign_to_supermedians_only {j in N, k in N}:
    y[j,k] <= y[k,k];

subject to supermedian_has_one_median_in_each_period {k in N, t in T}:
    sum {j in N} z[j,k,t] = y[k,k];

subject to define_z_1 {j in N, k in N, t in T}:
    z[j,k,t] <= x[j,j,t];

subject to define_z_2 {j in N, k in N, t in T}:
    z[j,k,t] <= y[j,k];

subject to define_z_3 {j in N, k in N, t in T}:
    z[j,k,t] >= x[j,j,t] + y[j,k] - 1;