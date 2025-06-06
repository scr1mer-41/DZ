#include <sstream>
#include <unordered_map>
#include <string>
#include <vector>
#include <iostream>
#include <fstream>

#include "Driver.h"
#include "Error.h"

std::vector<char> Driver::inputChain;
char Driver::ch;

unsigned int Driver::line = 0;
unsigned int Driver::position = 0;

char Driver::chSpace = ' ';
char Driver::chTab = '\t';
char Driver::chEOL = '\r';
char Driver::chEOT = '\0';

Driver::Driver(std::shared_ptr<Error> errorPtr) : errorPtr(std::move(errorPtr)) {}

void Driver::resetText(char const* path)
{
    std::ifstream file(path);

    if (!file)
    {
        errorPtr->error("Не удалось открыть файл");
    }

    std::stringstream buffer;
    buffer << file.rdbuf();
    std::string content = buffer.str();
    Driver::inputChain.assign(content.begin(), content.end());

    file.close();
    return;
}

void Driver::nextCh()
{
    if (inputChain.begin() != inputChain.end())
    {
        Driver::ch = inputChain[0];
        inputChain.erase(inputChain.begin());
        Driver::position++;

        std::cout << Driver::ch;

        if (Driver::ch == '\n' || Driver::ch == '\r')
        {
            Driver::ch = chEOL;
            Driver::position = 0;
        }
    }
    else
    {
        Driver::ch = chEOT;
    }

    return;
}