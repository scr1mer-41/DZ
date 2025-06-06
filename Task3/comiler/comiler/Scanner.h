#pragma once

#include <unordered_map>
#include <string>
#include <vector>

#include "Driver.h"

class Scanner
{
public:
    enum class Lex
    {
        NONE, // Тип лексемы в которую входят все лексемы из Оберона
        NAME,
        NUM,
        MODULE,
        IMPORT,
        BEGIN,
        END,
        CONST,
        VAR,
        WHILE,
        DO,
        IF,
        THEN,
        ELSIF,
        ELSE,
        MULT,
        DIV,
        MOD,
        PLUS,
        MINUS,
        EQ,
        NE,
        LT,
        LE,
        GT,
        GE,
        DOT,
        COMMA,
        COLON,
        SEMI,
        ASS,
        LPAR,
        RPAR,
        EOT,
        FOR,
        TO,
        BY,
        REPEAT,
        UNTIL
    };
    static Lex lex;
    static int numValue;
    static unsigned int lexPosition;
    static std::string nameValue;

    Scanner(Driver& driver, std::shared_ptr<Error> errorPtr);
    void nextLex();
    void scanName();
    void scanNumber();
    void skipComment();
    std::string getStringNameOfLex(Scanner::Lex lex);

private:
    Driver driver;
    std::shared_ptr<Error> errorPtr;

    // служебные слова
    std::unordered_map<std::string, Lex> lexTable = {
        {"MODULE", Lex::MODULE},
        {"IMPORT", Lex::IMPORT},
        {"CONST", Lex::CONST},
        {"VAR", Lex::VAR},
        {"BEGIN", Lex::BEGIN},
        {"END", Lex::END},
        {"IF", Lex::IF},
        {"THEN", Lex::THEN},
        {"ELSIF", Lex::ELSIF},
        {"ELSE", Lex::ELSE},
        {"WHILE", Lex::WHILE},
        {"DO", Lex::DO},
        {"DIV", Lex::DIV},
        {"MOD", Lex::MOD},
        {"ARRAY", Lex::NONE},
        {"RECORD", Lex::NONE},
        {"POINTER", Lex::NONE},
        {"SET", Lex::NONE},
        {"WITH", Lex::NONE},
        {"CASE", Lex::NONE},
        {"OF", Lex::NONE},
        {"LOOP", Lex::NONE},
        {"EXIT", Lex::NONE},
        {"PROCEDURE", Lex::NONE},

        {"IN", Lex::NONE},
        {"IS", Lex::NONE},
        {"NIL", Lex::NONE},
        {"OR", Lex::NONE},
        {"TYPE", Lex::NONE},
        {"RETURN", Lex::NONE},


        {"FOR", Lex::FOR},
        {"TO", Lex::TO},
        {"BY", Lex::BY},
        {"REPEAT", Lex::REPEAT},
        {"UNTIL", Lex::UNTIL}
    };

    std::unordered_map<Lex, std::string> lexToStr = {
        {Lex::NAME, "имя"},
        {Lex::NUM, "число"},

        {Lex::DIV, "DIV"},
        {Lex::MOD, "MOD"},
        {Lex::MODULE, "MODULE"},
        {Lex::IMPORT, "IMPORT"},
        {Lex::BEGIN, "BEGIN"},
        {Lex::END, "END"},
        {Lex::CONST, "CONST"},
        {Lex::VAR, "VAR"},
        {Lex::WHILE, "WHILE"},
        {Lex::DO, "DO"},
        {Lex::IF, "IF"},
        {Lex::THEN, "THEN"},
        {Lex::ELSIF, "ELSIF"},
        {Lex::ELSE, "ELSE"},

        {Lex::REPEAT, "REPEAT"},
        {Lex::FOR, "FOR"},
        {Lex::TO, "TO"},
        {Lex::BY, "BY"},
        {Lex::UNTIL, "UNTIL"},

        {Lex::MULT, "*"},
        {Lex::PLUS, "+"},
        {Lex::MINUS, "-"},
        {Lex::EQ, "="},
        {Lex::NE, "#"},
        {Lex::LT, "<"},
        {Lex::LE, "<="},
        {Lex::GT, ">"},
        {Lex::GE, ">="},
        {Lex::DOT, "."},
        {Lex::COMMA, ","},
        {Lex::COLON, ":"},
        {Lex::SEMI, ";"},
        {Lex::ASS, ":="},
        {Lex::LPAR, "("},
        {Lex::RPAR, ")"},
        {Lex::EOT, "конец текста"}
    };
};