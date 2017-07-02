#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)

uchar4 __attribute__((kernel)) grayscale_average(uchar4 pixel_in, uint32_t x, uint32_t y){
    uchar4 pixel_out = pixel_in;
    int average = (float)((float)(pixel_in.r + pixel_in.g+pixel_in.b)/3.0);

    pixel_out.r = pixel_out.g = pixel_out.b = average;
    return pixel_out;
}