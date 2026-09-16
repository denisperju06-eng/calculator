CC = gcc
CFLAGS = -Wall -Wextra -O2 -fPIC

UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Darwin)
    TARGET = mylib.dylib
    LDFLAGS = -dynamiclib
else
    TARGET = mylib.so
    LDFLAGS = -shared
endif

all: $(TARGET)

$(TARGET): mylib.c mylib.h
	$(CC) $(CFLAGS) $(LDFLAGS) -o $@ $<

run: $(TARGET)
	python3 main.py

clean:
	rm -f mylib.dylib mylib.so mylib.dll
