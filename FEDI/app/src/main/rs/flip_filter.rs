#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

rs_allocation img_in;   // alokacja wejsciowa
int32_t width, height;  // szerokosc i wysokość
int direction;          // kierunek obrotu

// inicjalizacja
void setup(){
    width = rsAllocationGetDimX(img_in);
    height = rsAllocationGetDimY(img_in);
}

// kernel odpowiedzialny za zmiane polozenia piksela SPRWDZIC INDEKSY (-1)
uchar4 __attribute__((kernel)) flip_filter(uchar4 in, uint32_t x, uint32_t y){
    float4 rgba;
    if(!direction)
        rgba = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, width-x-1, y));
    else if(direction == 1)
        rgba = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, x, height - y-1));
    else if(direction == 2)
        rgba = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, width-y-1, x));
    else if(direction == 3)
        rgba = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, y, height - x-1));

    return rsPackColorTo8888(rgba.rgb);
}