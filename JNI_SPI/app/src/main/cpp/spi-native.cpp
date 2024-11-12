// app/src/main/cpp/spi-native.cpp
#include <jni.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <linux/spi/spidev.h>
#include <android/log.h>

#define TAG "SPI_Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static int spi_fd = -1;
static const char *device = "/dev/spidev0.0";
static uint8_t mode = SPI_MODE_0;
static uint8_t bits = 8;
static uint32_t speed = 500000;

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_jni_1spi_SPIManager_nativeInit(JNIEnv *env, jobject thiz) {
    spi_fd = open(device, O_RDWR);
    if (spi_fd < 0) {
        LOGE("Failed to open SPI device: %s", device);
        return;
    }
    LOGI("Successfully opened SPI device");

    // Set SPI mode
    if (ioctl(spi_fd, SPI_IOC_WR_MODE, &mode) < 0) {
        LOGE("Failed to set SPI mode");
        return;
    }

    // Set bits per word
    if (ioctl(spi_fd, SPI_IOC_WR_BITS_PER_WORD, &bits) < 0) {
        LOGE("Failed to set bits per word");
        return;
    }

    // Set max speed
    if (ioctl(spi_fd, SPI_IOC_WR_MAX_SPEED_HZ, &speed) < 0) {
        LOGE("Failed to set max speed");
        return;
    }

    LOGI("SPI initialized successfully");
}

JNIEXPORT jbyteArray JNICALL
Java_com_example_jni_1spi_SPIManager_nativeRead(JNIEnv *env, jobject thiz) {
    uint8_t tx_buffer[2] = {0x01, 0x00}; // Command to request data
    uint8_t rx_buffer[2] = {0};

    struct spi_ioc_transfer tr = {
            .tx_buf = (unsigned long) tx_buffer,
            .rx_buf = (unsigned long) rx_buffer,
            .len = 2,
            .speed_hz = speed,
            .bits_per_word = bits,
            .delay_usecs = 0,
            .cs_change = 0,
    };

    if (ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr) < 1) {
        LOGE("Failed to send SPI message");
        return NULL;
    }

    LOGI("SPI Read: rx_buffer[0]=%d, rx_buffer[1]=%d", rx_buffer[0], rx_buffer[1]);

    jbyteArray result = env->NewByteArray(2);
    if (result == NULL) {
        LOGE("Failed to create byte array");
        return NULL;
    }

    env->SetByteArrayRegion(result, 0, 2, (jbyte *) rx_buffer);
    return result;
}

JNIEXPORT void JNICALL
Java_com_example_jni_1spi_SPIManager_nativeClose(JNIEnv *env, jobject thiz) {
    if (spi_fd >= 0) {
        close(spi_fd);
        spi_fd = -1;
        LOGI("SPI device closed");
    }
}


}