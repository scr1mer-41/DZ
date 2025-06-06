#include <unordered_map>
#include <string>
#include <vector>

#include "Scanner.h"
#include "Error.h"

Scanner::Lex Scanner::lex = Scanner::Lex::NONE;
std::string Scanner::nameValue = "";
int Scanner::numValue = 0;
unsigned int Scanner::lexPosition = 0;

Scanner::Scanner(Driver& driver, std::shared_ptr<Error> errorPtr) :
    driver(driver), errorPtr(std::move(errorPtr)) {
    driver.nextCh();
}

void Scanner::nextLex()
{
    while (Driver::ch == Driver::chSpace ||
        Driver::ch == Driver::chTab ||
        Driver::ch == Driver::chEOL)
    {
        driver.nextCh();
    }

    Scanner::lexPosition = Driver::position;

    if (Driver::ch >= 'A' && Driver::ch <= 'Z' || Driver::ch >= 'a' && Driver::ch <= 'z')
    {
        scanName();
    }
    else if (Driver::ch >= '0' && Driver::ch <= '9')
    {
        scanNumber();
    }
    else if (Driver::ch == ';')
    {
        lex = Lex::SEMI;
        driver.nextCh();
    }
    else if (Driver::ch == ':')
    {
        driver.nextCh();
        if (Driver::ch == '=')
        {
            lex = Lex::ASS;
            driver.nextCh();
        }
        else
        {
            lex = Lex::COLON;
        }
    }
    else if (Driver::ch == '.')
    {
        lex = Lex::DOT;
        driver.nextCh();
    }
    else if (Driver::ch == ',')
    {
        lex = Lex::COMMA;
        driver.nextCh();
    }
    else if (Driver::ch == '+')
    {
        lex = Lex::PLUS;
        driver.nextCh();
    }
    else if (Driver::ch == '-')
    {
        lex = Lex::MINUS;
        driver.nextCh();
    }
    else if (Driver::ch == '*')
    {
        lex = Lex::MULT;
        driver.nextCh();
    }
    else if (Driver::ch == '(')
    {
        driver.nextCh();
        if (Driver::ch == '*')
        {
            skipComment();
            nextLex();
        }
        else
        {
            lex = Lex::LPAR;
        }
    }
    else if (Driver::ch == ')')
    {
        lex = Lex::RPAR;
        driver.nextCh();
    }
    else if (Driver::ch == '=')
    {
        lex = Lex::EQ;
        driver.nextCh();
    }
    else if (Driver::ch == '#')
    {
        lex = Lex::NE;
        driver.nextCh();
    }
    else if (Driver::ch == '<')
    {
        driver.nextCh();
        if (Driver::ch == '=')
        {
            lex = Lex::LE;
            driver.nextCh();
        }
        else
        {
            lex = Lex::LT;
        }
    }
    else if (Driver::ch == '>')
    {
        driver.nextCh();
        if (Driver::ch == '=')
        {
            lex = Lex::GE;
            driver.nextCh();
        }
        else
        {
            lex = Lex::GT;
        }
    }
    else if (Driver::ch == Driver::chEOT)
    {
        lex = Lex::EOT;
    }
    else
    {
        errorPtr->lexError("������������ ������");
    }
}

void Scanner::scanName()
{
    nameValue = "";

    while (Driver::ch >= 'A' && Driver::ch <= 'Z' ||
        Driver::ch >= 'a' && Driver::ch <= 'z' ||
        Driver::ch > '0' && Driver::ch < '9')
    {
        nameValue.push_back(Driver::ch);
        driver.nextCh();
    }

    // std::cout << nameValue;

    auto it = lexTable.find(nameValue);
    lex = (it != lexTable.end()) ? it->second : Lex::NAME;

}

void Scanner::scanNumber()
{
    numValue = 0;

    while (Driver::ch >= '0' && Driver::ch <= '9')
    {
        int digit = Driver::ch - '0';
        if (numValue <= (INT_MAX - digit) / 10)
        {
            numValue = numValue * 10 + digit;
        }
        else
        {
            errorPtr->lexError("����� ��������� ����������� ���������");
        }

        driver.nextCh();
    }

    lex = Lex::NUM;
}

void Scanner::skipComment()
{
    driver.nextCh();
    while (true)
    {
        while (Driver::ch != '*' && Driver::ch != Driver::chEOT)
        {
            driver.nextCh();

            if (Driver::ch == '(')
            {
                driver.nextCh();
                if (Driver::ch == '*')
                {
                    skipComment();
                }
            }
        }

        if (Driver::ch == Driver::chEOT)
        {
            errorPtr->lexError("��� ����� �����������");
        }
        else
        {
            driver.nextCh();
        }

        if (Driver::ch == ')')
        {
            break;
        }
    }
    driver.nextCh();
}

std::string Scanner::getStringNameOfLex(Scanner::Lex lex)
{
    auto it = lexToStr.find(lex);
    std::string name = (it != lexToStr.end()) ? it->second : "������� �� �������";

    return name;
}