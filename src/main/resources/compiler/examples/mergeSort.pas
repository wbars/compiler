program mergeSort;
type intArr = array of Integer;
var a: intArr;


procedure sortMerge(a,l,r: intArr);
var i,j,k: Integer;

begin
    i := 0;
    j := 0;
    k := 0;

    while ( (i < len(l)) && (j < len(r) ) ) do
     begin
        if (l[i] < r[j]) then begin
            array_push(l[i], k, a);
            i := i + 1;
            k := k + 1;
        end
        else begin
            array_push(r[j], k, a);
            j := j + 1;
            k:= k + 1;
         end
        end;


     while (i < len(l)) do
     begin
        array_push(l[i], k, a);
        i := i + 1;
        k := k + 1;
     end;

     while (j < len(r)) do
          begin
             array_push(r[j], k, a);
             j := j + 1;
             k := k + 1;
          end;
end;

procedure division(a: intArr);
var low, high, mid, p: Integer; l,r: intArr;

begin
    low := 0;
    high := len(a);
    p := 0;
    mid := (high + low) / 2;
    if (len(a) < 2) then return;
    l := new_array(mid , Integer);
    r := new_array((high - mid), Integer);

    for p := 0 to (len(l) - 1) do array_push(a[p], p, l);
    for q := 0 to (len(r) - 1) do begin
        array_push(a[p], q, r);
        p := p + 1;
    end;
    division(l);
    division(r);
    sortMerge(a, l, r);
end;

procedure printArray(a: intArr);
begin
    for i := 0 to (len(a) - 1) do begin
     write(a[i]);
     write(' ');
     end;
     writeln(' ');
end;

begin
a := {1257, 9654, 3316, 2765, 8870, 1244, 2117, 6786, 4404, 5172, 7042, 6769, 4694, 3182, 5692, 3145, 9076, 6872, 4717, 5736, 3733, 5276, 3251, 382, 2583, 4370, 9967, 949, 8358, 1447, 176, 3748, 4603, 1679, 3, 7544, 8467, 3839, 4938, 3431, 6746, 7104, 254, 6434, 7161, 7316, 2312, 8764, 7472, 6575, 2780, 7539, 4821, 6693, 8253, 5405, 471, 1194, 3989, 5066, 861, 3889, 8080, 241, 8890, 2663, 2667, 1768, 5560, 2371, 7081, 4366, 7120, 9050, 6187, 4020, 8063, 2078, 5835, 6915, 894, 1569, 6543, 4158, 961, 9490, 4307, 6443, 9849, 3443, 138, 2372, 5041, 1253, 5470, 6769, 51, 874, 1128, 1478, 2425, 5507, 8224, 2766, 3026, 9278, 3012, 3846, 730, 7441, 6036, 8920, 3462, 5974, 3785, 5684, 3572, 8111, 9334, 8806, 5400, 6819, 4961, 179, 201, 4717, 8362, 8313, 1438, 5836, 8243, 9140, 9579, 5957, 5267, 5783, 3797, 6308, 2060, 3597, 6208, 5828, 7432, 4054, 4979, 5426, 684, 2275, 5727, 6482, 9333, 8974, 2428, 9932, 6908, 9690, 3272, 798, 9794, 6668, 2218, 3743, 2398, 2017, 2946, 2982, 9385, 6871, 6749, 9917, 7750, 2667, 7994, 2676, 4744, 1468, 6430, 5355, 4056, 2535, 6687, 3351, 6075, 3784, 7525, 6247, 3901, 1949, 383, 1217, 8953, 4613, 201, 4157, 49, 7163, 8805, 5266, 4355, 4967, 5572, 8868, 7689, 792, 2631, 6143, 1596, 7461, 5333, 2773, 6639, 7832, 4606, 9824, 1749, 1465, 6316, 3632, 5985, 9848, 278, 7881, 6269, 4051, 3813, 7037, 927, 3406, 4194, 1819, 631, 5511, 1971, 5744, 459, 6902, 3919, 1171, 7462, 5845, 6094, 7459, 1400, 9422, 6038, 7328, 7470, 9943, 2935, 2025, 27, 8265, 1514, 5275, 4475, 2507, 882, 3489, 396, 9579, 1928, 2740, 1490, 3284, 6780, 3537, 3656, 9674, 8924, 2459, 2052, 9358, 8209, 6464, 1421, 4913, 9292, 7158, 3472, 6065, 7354, 4741, 4575, 2832, 7015, 5280, 5408, 7839, 3282, 4932, 1493, 5759, 2412, 7020, 5554, 2481, 2554, 2110, 305, 7868, 9002, 7310, 916, 9988, 4633, 3445, 8162, 1976, 3800, 7876, 8592, 6783, 8670, 5536, 4932, 6038, 6661, 5345, 9363, 1981, 3929, 2727, 5044, 8145, 576, 2550, 5820, 1590, 9337, 3281, 2990, 6479, 8946, 2209, 8489, 3204, 1260, 2205, 1863, 1257, 4042, 5197, 6496, 7276, 704, 1776, 3263, 710, 1748, 4779, 8588, 9568, 4140, 5699, 7893, 6692, 4275, 1195, 8657, 2158, 119, 476, 1782, 5365, 1258, 4775, 4180, 8548, 5377, 1960, 3326, 5201, 7488, 1273, 2072, 4627, 9934, 2327, 3034, 7019, 9588, 9383, 351, 1890, 7186, 5081, 688, 6556, 7224, 368, 7698, 3592, 576, 3418, 7214, 6310, 3755, 8386, 4469, 3033, 3230, 2132, 2900, 753, 1029, 5094, 6371, 3369, 9231, 1031, 6818, 1943, 3183, 4687, 8587, 3410, 2535, 9738, 4017, 6814, 6041, 7739, 939, 6570, 4894, 432, 8387, 1407, 7310, 3450, 3612, 6680, 5395, 6067, 8750, 5082, 1140, 2225, 9584, 8296, 4912, 8877, 26, 7553, 4196, 9115, 2224, 9379, 8432, 9782, 9384, 8894, 7102, 7545, 6663, 8701, 8731, 6216, 1617, 3416, 7635, 8377, 3925, 6212, 3762, 616, 1422, 4873, 2111, 2742, 1331, 3529, 8689, 3158, 153, 4425, 3341, 1804, 1477, 48, 3370, 6778, 9311, 6619, 8149, 6729, 2044, 9314, 2341, 5332, 9641, 9980, 2718, 7884, 4793, 883, 5861, 1549, 977, 3192, 1511, 8190, 2335, 6778, 3003, 8964, 2636, 8561, 2343, 6333, 142, 3582, 8621, 2616, 191, 5095, 1932, 5695, 5658, 2155, 7751, 7487, 9605, 7061, 4463, 347, 7059, 5743, 7153, 7556, 2768, 2485, 3445, 1197, 3436, 7997, 9950, 740, 5379, 7674, 1629, 604, 3985, 5023, 6546, 7549, 8377, 3621, 1647, 3088, 580, 2254, 3709, 9038, 7323, 4786, 8383, 3824, 5646, 5883, 9288, 1227, 8683, 3276, 1361, 9014, 5763, 6036, 8221, 2982, 3420, 4885, 1585, 732, 6449, 2281, 4356, 5170, 1954, 3743, 1992, 634, 8581, 9427, 1319, 4366, 9024, 61, 3570, 1808, 6131, 7494, 7322, 5965, 8818, 3666, 2830, 6236, 5833, 182, 3137, 5031, 7757, 2126, 4555, 385, 7519, 732, 7716, 4202, 7910, 637, 8268, 266, 356, 9987, 9362, 5574, 6842, 5209, 5667, 4833, 4499, 3505, 5009, 6673, 6531, 5799, 7557, 4555, 1170, 4492, 4484, 4340, 3142, 8613, 8578, 6903, 5282, 9401, 2078, 9434, 884, 3792, 1734, 2384, 1792, 4032, 6821, 4160, 8079, 3304, 318, 9506, 1917, 341, 1348, 7876, 1316, 1751, 1396, 8169, 6957, 6708, 7172, 975, 3870, 7331, 2320, 4732, 250, 9557, 7355, 7947, 2684, 3670, 4383, 9744, 7028, 8962, 4073, 9302, 7576, 6034, 3112, 8502, 7199, 8701, 6823, 4163, 5660, 9696, 2700, 2738, 87, 4263, 2493, 7714, 815, 6598, 1394, 2530, 9215, 695, 7132, 8777, 4120, 1930, 6044, 8842, 790, 2032, 1364, 8242, 8011, 6833, 8906, 2609, 4734, 586, 4986, 1887, 3002, 9647, 2191, 5026, 9295, 6412, 4251, 9118, 3342, 9493, 7382, 9643, 9837, 1422, 5334, 5919, 4450, 1073, 3882, 7802, 6818, 936, 7558, 8559, 5828, 3888, 3885, 9202, 2597, 5138, 6969, 4853, 1322, 4281, 7754, 693, 6239, 3239, 8565, 6428, 3191, 7342, 4486, 866, 1611, 2696, 2057, 3250, 2701, 8253, 8507, 9273, 2097, 4744, 4832, 9224, 4535, 1734, 1189, 7926, 7996, 7624, 7620, 587, 5734, 1005, 2213, 2663, 5526, 6540, 9615, 2630, 7744, 6390, 7547, 1150, 8631, 8714, 6696, 9059, 2498, 8228, 504, 614, 7442, 6998, 7995, 2708, 5125, 1567, 4162, 6423, 8358, 3359, 2638, 3125, 9353, 8957, 2783, 5284, 4449, 1848, 4887, 3867, 1452, 7732, 6108, 4208, 6599, 7307, 1458, 3277, 499, 4797, 7911, 1532, 9088, 7477, 3166, 2575, 3206, 2519, 4909, 7732, 6786, 7933, 3596, 5794, 6761, 8952, 1307, 4512, 5607, 7717, 2581, 3742, 9233, 3019, 2890, 9398, 1349, 570, 9484, 8076, 6828, 8162, 3673, 86, 1167, 9058, 9916, 2197, 3896, 6440, 2751, 4424, 1386, 6024, 6032, 362, 9668, 4552, 930, 7903, 337, 4808, 592, 4786, 5543, 6870, 9016, 8074, 8696, 6840, 396, 7203, 461, 4863, 6801, 8915, 1984, 7104, 5528, 3378, 3186, 2298, 3253, 3868, 5821, 4648, 8322, 802, 5099, 9833, 5676, 143, 4075, 5633, 7821, 1041, 2640, 6476, 8800, 8664, 290, 29, 7678, 5569, 9384, 3789, 3448, 5409, 5889, 4740, 9957, 2750, 9327, 9098, 1651, 7514, 2595, 1249, 6427, 4806, 4000, 7559, 633, 7731, 8637, 2754, 5619, 4329, 7415, 4668, 5483, 4276, 5972, 9481, 1699, 5653, 5501, 1859, 9740, 2889, 340, 1770, 7726, 6759, 9358, 8210, 6180, 6271, 8373, 421, 9856, 62, 3020, 2272, 6167, 9123, 6503, 6910, 8493, 349, 4464, 4498, 2166, 6958, 2466, 2344, 3861, 8080, 2886, 9777, 1400, 7307, 7458, 7425};
write('Array before sorting: ');
printArray(a);
division(a);
write('Array after sorting: ');
printArray(a);
end.