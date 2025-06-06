import re


class Token:
    def __init__(self, type, value, position):
        self.type = type
        self.value = value
        self.position = position
        self.args_for_func = 0
    def __repr__(self):
        return f'Token(type: {self.type}, value: {self.value}, position: {self.position}, args_for_func: {self.args_for_func})'

class Lexer:
    def __init__(self):
        self.text = ''
        self.pos = 0
        self.current_char = self.text[self.pos] if self.text else None
        self.tokens = []

    def getTokens(self, text):
        self.text = text
        self.pos = 0
        self.current_char = self.text[self.pos] if self.text else None
        self.tokens = []
        self.tokenize()
        print("Lexer: the expression is true.")
        return self.tokens

    def error(self, msg):
        raise Exception(f'Lexer error: {msg}')

    def next_pos(self):
        self.pos += 1
        if self.pos < len(self.text):
            self.current_char = self.text[self.pos]
        else:
            self.current_char = None

    def skip_space(self):
        while self.current_char is not None and self.current_char.isspace():
            self.next_pos()

    def number(self):
        result = ''
        start_pos = self.pos

        while self.current_char is not None and (self.current_char.isdigit() or self.current_char == '.'):
            result += self.current_char
            self.next_pos()
        if (result[0] == '.') or (result[-1] == '.'):
            self.error(f"Invalid number at position {start_pos}")


        return Token('NUMBER', float(result), start_pos)

    def word(self):
        result = ""
        start_pos = self.pos
        while self.current_char is not None and self.current_char.isalpha():
            result += self.current_char
            self.next_pos()
        return Token('WORD', result, start_pos)

    def tokenize(self):
        while self.current_char is not None:
            if self.current_char.isspace():
                self.skip_space()
                continue
            if self.current_char.isdigit() or (self.current_char == '.'):
                self.tokens.append(self.number())
            elif self.current_char.isalpha():
                self.tokens.append(self.word())
            elif self.current_char == '+':
                self.tokens.append(Token('OPERATOR', '+', self.pos))
                self.next_pos()
            elif self.current_char == '-':
                self.tokens.append(Token('OPERATOR', '-', self.pos))
                self.next_pos()
            elif self.current_char == '*':
                self.tokens.append(Token('OPERATOR', '*', self.pos))
                self.next_pos()
            elif self.current_char == '/':
                self.tokens.append(Token('OPERATOR', '/', self.pos))
                self.next_pos()
            elif self.current_char == '(':
                self.tokens.append(Token('LPAREN', '(', self.pos))
                self.next_pos()
            elif self.current_char == ')':
                self.tokens.append(Token('RPAREN', ')', self.pos))
                self.next_pos()
            elif self.current_char == ',':
                self.tokens.append(Token('COMMA', ',', self.pos))
                self.next_pos()
            elif self.current_char == '^':
                self.tokens.append(Token('OPERATOR', '^', self.pos))
                self.next_pos()
            else:
                self.error(f"Unexpected character '{self.current_char}' at position {self.pos}")
        self.tokens.append(Token('EOF', '', self.pos))

class Parser:
    def __init__(self):
        self.tokens = []
        self.pos = 0
        self.current_token = None

    def eat(self, token_type):
        if self.current_token.type == token_type:
            self.pos += 1
            if self.pos < len(self.tokens):
                self.current_token = self.tokens[self.pos]
            else:
                self.current_token = Token('EOF', '', self.tokens[-1].position + 1)
        else:
            self.error(f"Expected {token_type}, got {self.current_token.type}")

    def error(self, msg):
        raise Exception(f'Syntax error at position {self.current_token.position}: {msg}')

    def parse_expression(self):
        while self.current_token.type == 'OPERATOR' and self.current_token.value in ('+', '-'):
            self.eat('OPERATOR')

        if self.current_token.type == 'NUMBER':
            self.eat('NUMBER')
            if self.current_token.type == 'OPERATOR':
                self.eat('OPERATOR')
                self.parse_expression()

        # elif self.current_token.type == 'WORD':
        #     self.eat('WORD')
        #
        #     if self.current_token.type == 'LPAREN':
        #         self.eat('LPAREN')
        #         self.parse_expression()
        #         while self.current_token.type == 'COMMA':
        #             self.eat('COMMA')
        #             self.parse_expression()
        #         self.eat('RPAREN')
        #
        #     if self.current_token.type == 'OPERATOR':
        #         self.eat('OPERATOR')
        #         self.parse_expression()


        elif self.current_token.type == 'WORD':
            if self.tokens[self.pos+1].type == 'LPAREN':

                self.current_token.type = 'FUNC'
                pos_func = self.pos
                self.eat('FUNC')
                self.eat('LPAREN')

                self.parse_expression()
                self.tokens[pos_func].args_for_func += 1
                while self.current_token.type == 'COMMA':
                    self.eat('COMMA')
                    self.parse_expression()
                    self.tokens[pos_func].args_for_func += 1
                self.eat('RPAREN')
            else:
                self.eat('WORD')

            if self.current_token.type == 'OPERATOR':
                self.eat('OPERATOR')
                self.parse_expression()


        elif self.current_token.type == 'LPAREN':
            self.eat('LPAREN')
            self.parse_expression()
            self.eat('RPAREN')

            if self.current_token.type == 'OPERATOR':
                self.eat('OPERATOR')
                self.parse_expression()

        else:
            if self.current_token.type == 'EOF':
                self.error(f"Expression expected after token {self.current_token}")
            elif self.current_token.type == 'RPAREN':
                self.error(f"The expression in brackets must not be empty")
            else:
                self.error(f"Unexpected token {self.current_token}")

    def parse(self, LexTokens):
        self.tokens = LexTokens
        self.pos = 0
        self.current_token = self.tokens[self.pos]

        while self.current_token.type != 'EOF':
            self.parse_expression()

        print("Parser: the expression is true")
        return self.tokens

class Dictionary:
    def __init__(self):
        self.priority = {
            '(':0,
            ')':1,
            '+':2,
            '-':2,
            '*':3,
            '/':3,
            'u-':4,
            '^':5
        }
    def getPriority(self, char):
        return self.priority.get(char)

class PostFixConvertor:
    def __init__(self):
        self.dictionary = Dictionary()
        self.stack = []
        self.output = []

    def getPostFixString(self, ParsedTokens):
        self.stack = []
        self.output = []

        tokens = ParsedTokens
        i = 0
        n = len(tokens)
        while i < n:
            token = tokens[i]
            if (token.value in ('+', '-')) and ((i == 0) or (tokens[i-1].type in ('OPERATOR', 'LPAREN', 'COMMA'))):
                token.value = 'u' + token.value

            if token.type == 'NUMBER':
                self.output.append(token)
            elif (token.type == 'WORD') and ((i+1 < n) and (tokens[i+1].type != 'LPAREN')):
                self.output.append(token)
            elif (token.type == 'FUNC') and ((i+1 < n) and (tokens[i+1].type == 'LPAREN')):
                self.stack.append(token)
            elif token.type == 'LPAREN':
                self.stack.append(token)
            elif token.type == "COMMA":
                while (self.stack[-1].type != 'LPAREN'):
                    self.output.append(self.stack.pop())
            elif token.type == 'RPAREN':
                while (self.stack) and (self.stack[-1].type != 'LPAREN'):
                    self.output.append(self.stack.pop())
                self.stack.pop()
            elif token.type == 'OPERATOR':
                while ((len(self.stack) != 0) and ((self.stack[-1].type in ('WORD', 'FUNC')) or (self.dictionary.getPriority(token.value) <= self.dictionary.getPriority(self.stack[-1].value)))):
                    self.output.append(self.stack.pop())
                self.stack.append(token)
            i += 1

        while self.stack:
            self.output.append(self.stack.pop())

        return self.output

class Evaluate:
    def __init__(self):
        self.tokens = []
        self.stack = []

    def limit_on_number(self, number):
        for i in str(number).split("."):
            if len(i) >= 17:
                self.Error(f"Число {number} превысило ограничение в 16 цифр в целой части или в остатке.")

    def Error(self, msg):
        raise Exception(f'Calculation error: {msg}')

    def Calc(self, token):
        result = 0

        if token.type == 'OPERATOR':
            if token.value in ('+', '-', '*', '/', '^'):
                b = self.stack.pop()
                a = self.stack.pop()
                operator = token.value
                if operator == '+':
                    self.stack.append(a+b)
                elif operator == '-':
                    self.stack.append(a-b)
                elif operator == '*':
                    self.stack.append(a*b)
                elif operator == '/':
                    self.stack.append(a/b)
                elif operator == '^':
                    self.stack.append(a**b)
            elif token.value == 'u-':
                a = self.stack.pop()
                self.stack.append(-a)
        elif token.type == 'WORD':

            res = float(input(f"Введите значение переменной {token.value}: "))
            self.limit_on_number(res)
            self.stack.append(res)
        elif token.type == 'FUNC':
            args = '('
            n = token.args_for_func
            for i in range(n-1,-1,-1):
                if i != 0:
                    args += str(self.stack[-1-i]) + ','
                else:
                    args += str(self.stack[-1-i]) + ')'
            for i in range(n):
                self.stack.pop()

            function = token.value + args

            res = float(input(f"Введите значение функции {function}: "))
            self.limit_on_number(res)

            self.stack.append(float(res))
        elif token.type == 'NUMBER':
            self.limit_on_number(token.value)
            self.stack.append(token.value)

    def evaluate(self, PostFixArrayOfTokens):
        self.tokens = PostFixArrayOfTokens
        self.stack.clear()

        for token in self.tokens:
            self.Calc(token)
        return self.stack.pop()


text = input("Введите выражение:")

try:
    lexer = Lexer()
    Tokens = lexer.getTokens(text)


    parser = Parser()
    parsedTokens = parser.parse(Tokens)

    postfix = PostFixConvertor()
    postfixTokens = postfix.getPostFixString(parsedTokens)

    evaluate = Evaluate()
    print(evaluate.evaluate(postfixTokens))

except Exception as e:
    print(e)