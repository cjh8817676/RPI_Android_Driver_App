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
static uint8_t mode = SPI_MODE_3;
static uint8_t bits = 8;
static uint32_t speed = 100000;

#define START_COMMAND 0xA5
#define BUFFER_SIZE 1

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
Java_com_example_jni_1spi_SPIManager_nativeRead(JNIEnv *env, jobject thiz, jboolean isStartCommand) {
    if (spi_fd < 0) {
        LOGE("SPI device not initialized");
        return nullptr;
    }

    struct spi_ioc_transfer tr = {0};
    uint8_t tx_buffer[BUFFER_SIZE] = {0};
    uint8_t rx_buffer[BUFFER_SIZE] = {0};

    // 如果是起始命令，發送特殊的啟動碼
    if (isStartCommand) {
        tx_buffer[0] = START_COMMAND;
    } else {
        tx_buffer[0] = 0x00;  // 發送空字節來觸發讀取
    }

    tr.tx_buf = (unsigned long)tx_buffer;
    tr.rx_buf = (unsigned long)rx_buffer;
    tr.len = BUFFER_SIZE;
    tr.delay_usecs = 0;
    tr.speed_hz = speed;
    tr.bits_per_word = bits;

    int ret = ioctl(spi_fd, SPI_IOC_MESSAGE(1), &tr);
    if (ret < 1) {
        LOGE("Failed to send SPI message");
        return nullptr;
    }

    // 創建返回的 byte array
    jbyteArray result = env->NewByteArray(BUFFER_SIZE);
    if (result == nullptr) {
        return nullptr;
    }

    env->SetByteArrayRegion(result, 0, BUFFER_SIZE, (jbyte *)rx_buffer);
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