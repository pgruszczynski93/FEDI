#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

rs_allocation img_in;   // alokacja wejściowa
int32_t width, height;  // szerokość i wyskość obrazu
static float y_direction[9] = { 1, 2, 1, 0, 0, 0, -1, -2, -1}, x_direction[9] = { 1, 0, -1, 2, 0, -2, 1, 0, -1}; // maski określające kierunki krawędzi

//inicjalizacja zmiennych
void setup(){
    width = rsAllocationGetDimX(img_in);
    height = rsAllocationGetDimY(img_in);
}

// kernel wykrywający krawędzie, obliczający wartość piksela wedlug algorytmu Krzyż Robertsa
uchar4 __attribute__((kernel)) robers_filter(uchar4 in, uint32_t x, uint32_t y){
    float4 rgba = rsUnpackColor8888(in);
    float4 pix_1, pix_2, pix_3;
    if((x < width - 1) && (y < height - 1)){
        pix_1 = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, x+1, y));
        pix_2 = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, x, y+1));
        pix_3 = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, x+1, y+1));

        rgba.r = fabs(rgba.r - pix_3.r) + fabs(pix_1.r - pix_2.r);
        rgba.g = fabs(rgba.g - pix_3.g) + fabs(pix_1.g - pix_2.g);
        rgba.b = fabs(rgba.b - pix_3.b) + fabs(pix_1.b - pix_2.b);
    }
    return rsPackColorTo8888(rgba.rgb);
}

// kernel wykrywający krawędzie, obliczający wartość piksela wedlug algorytmu Sobela
uchar4 __attribute__((kernel)) sobel_filter(uchar4 in, uint32_t x, uint32_t y){
    float4 rgba ;
    uint32_t pos_x = x+1, pos_y = y+1;
    float3 x_dirs, y_dirs, xy_rgb;
    if(pos_x >= 1 && pos_x < width-1 && pos_y >= 1 && pos_y < height-1){
        int32_t kernel_index = 0;
        x_dirs = y_dirs = xy_rgb = 0;
        for(int32_t k=-1; k<=1; k++){
            for(int32_t l=-1; l<=1; l++){
                rgba = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, pos_x + l, pos_y + k));
                x_dirs.r += (rgba.r * x_direction[kernel_index]);
                x_dirs.g += (rgba.g * x_direction[kernel_index]);
                x_dirs.b += (rgba.b * x_direction[kernel_index]);

                y_dirs.r += (rgba.r * y_direction[kernel_index]);
                y_dirs.g += (rgba.g * y_direction[kernel_index]);
                y_dirs.b += (rgba.b * y_direction[kernel_index]);

                ++kernel_index;
            }
        }
        xy_rgb.r = fabs(x_dirs.r) + fabs(y_dirs.r);
        xy_rgb.g = fabs(x_dirs.g) + fabs(y_dirs.g);
        xy_rgb.b = fabs(x_dirs.b) + fabs(y_dirs.b);
        xy_rgb = Clamp01Float3(xy_rgb);
    }
    else{
        xy_rgb = 1.0;
    }
    return rsPackColorTo8888(xy_rgb);
}
