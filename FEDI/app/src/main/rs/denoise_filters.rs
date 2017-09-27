#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed


rs_allocation img_in; // alokacja wejsciowa
int32_t width, height;  // szerokość i wysokość
int32_t size;   // rozmiar

// inicjalizacja zmiennych
void setup(){
    width = rsAllocationGetDimX(img_in);
    height = rsAllocationGetDimY(img_in);
}

// kernel rozmywający - uśredniający wartość piksela na podstawie średniej z sąsiadów
uchar4 __attribute__((kernel)) average_filter(uchar4 in, uint32_t x, uint32_t y){
    float4 rgba;
    float3 rgb = rgba.rgb;
    uint32_t pos_x = x+1, pos_y = y+1;
    float val_r, val_g, val_b;

    if(pos_x >= (size+1) && pos_x < width-(size+1) && pos_y >= (size+1) && pos_y < height-(size+1)){
        val_r = val_g = val_b = 0.0f;
        for(int32_t k=-(size+1); k<=(size+1); k++){
            for(int32_t l=-(size+1); l<=(size+1); l++){
                rgba = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, pos_x + l, pos_y + k));
                val_r += rgba.r;
                val_g += rgba.g;
                val_b += rgba.b;
            }
        }
        float real_masksize = 2*(size+1)+1;
        val_r /= (real_masksize*real_masksize);
        val_g /= (real_masksize*real_masksize);
        val_b /= (real_masksize*real_masksize);
        rgb.r = val_r;
        rgb.g = val_g;
        rgb.b = val_b;
    }
    else{
        rgb = 1.0;
    }
    return rsPackColorTo8888(rgb);
}

// kernel rozmywający - uśredniający wartość piksela na podstawie mediany zbioru sąsiadów
uchar4 __attribute__((kernel)) median_filter(uchar4 in, uint32_t x, uint32_t y){
    float4 rgba;
    float3 rgb = rgba.rgb;
    int32_t real_masksize = 2*(size+1)+1;
    uint32_t pos_x = x+1, pos_y = y+1;

    float vals_r3[9], vals_r5[25];
    float vals_g3[9], vals_g5[25];
    float vals_b3[9], vals_b5[25];

    float* vals_r;
    float* vals_g;
    float* vals_b;

    if(!size){
        vals_r = vals_r3;
        vals_g = vals_g3;
        vals_b = vals_b3;
    }

    else{
        vals_r = vals_r5;
        vals_g = vals_g5;
        vals_b = vals_b5;
    }

    if(pos_x >= (size+1) && pos_x < width-(size+1) && pos_y >= (size+1) && pos_y < height-(size+1)){
        int32_t index = 0;
        for(int32_t k=-(size+1); k<=(size+1); k++){
            for(int32_t l=-(size+1); l<=(size+1); l++){
                rgba = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, pos_x + l, pos_y + k));
                vals_r[index] = rgba.r;
                vals_g[index] = rgba.g;
                vals_b[index++] = rgba.b;
            }
        }
        sort(vals_r,real_masksize);
        sort(vals_g,real_masksize);
        sort(vals_b,real_masksize);

        rgb.r = vals_r[real_masksize>>1];
        rgb.g = vals_g[real_masksize>>1];
        rgb.b = vals_b[real_masksize>>1];

    }
    else{
        rgb = 1.0;
    }
    return rsPackColorTo8888(rgb);
}