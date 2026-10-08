CXX = clang++
CXXFLAGS = -std=c++17 -Wall -Wextra -O2
TARGET = main
SRC = main.cpp

all: $(TARGET)

$(TARGET): $(SRC)
	$(CXX) $(CXXFLAGS) $(SRC) -o $(TARGET)

run: $(TARGET)
	./$(TARGET)

test: $(TARGET)
	./$(TARGET) --test

clean:
	rm -f $(TARGET) *.o

.PHONY: all run test clean
