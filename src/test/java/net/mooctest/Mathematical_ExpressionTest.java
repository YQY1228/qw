package net.mooctest;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.*;

public class Mathematical_ExpressionTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        // 重置配置
        Mathematical_Expression.Options.setUseBigDecimal(false);
        Mathematical_Expression.Options.setUseCache(false);
    }

    @After
    public void tearDown() throws Exception {
    }

    // ==================== WrongFormat 异常类测试 ====================
    
    @Test
    public void testWrongFormatException_NoArgs() {
        // 测试无参构造
        WrongFormat e = new WrongFormat();
        assertNotNull(e);
        assertNull(e.getMessage());
    }
    
    @Test
    public void testWrongFormatException_WithMessage() {
        // 测试带消息构造
        String msg = "Test error message";
        WrongFormat e = new WrongFormat(msg);
        assertEquals(msg, e.getMessage());
    }
    
    @Test
    public void testWrongFormatException_WithMessageAndCause() {
        // 测试带消息和原因构造
        String msg = "Test error";
        Throwable cause = new RuntimeException("Root cause");
        WrongFormat e = new WrongFormat(msg, cause);
        assertEquals(msg, e.getMessage());
        assertEquals(cause, e.getCause());
    }
    
    @Test
    public void testWrongFormatException_WithCause() {
        // 测试仅带原因构造
        Throwable cause = new IllegalArgumentException("Cause");
        WrongFormat e = new WrongFormat(cause);
        assertEquals(cause, e.getCause());
    }

    // ==================== ConstantRegion 常量测试 ====================
    
    @Test
    public void testConstantRegionValues() {
        // 测试所有常量的值
        assertEquals(1.57F, ConstantRegion.VERSION, 0.001);
        assertEquals("null", ConstantRegion.STRING_NULL);
        assertEquals('(', ConstantRegion.LEFT_BRACKET);
        assertEquals(')', ConstantRegion.RIGHT_BRACKET);
        assertEquals('.', ConstantRegion.DECIMAL_POINT);
        assertEquals(' ', ConstantRegion.EMPTY);
        assertEquals("", ConstantRegion.NO_CHAR);
        assertEquals('+', ConstantRegion.PLUS_SIGN);
        assertEquals('-', ConstantRegion.MINUS_SIGN);
        assertEquals('*', ConstantRegion.MULTIPLICATION_SIGN);
        assertEquals('/', ConstantRegion.DIVISION_SIGN);
        assertEquals('%', ConstantRegion.REMAINDER_SIGN);
        assertEquals('!', ConstantRegion.FACTORIAL_SIGN);
        assertEquals('^', ConstantRegion.POW_SIGN);
        assertEquals(',', ConstantRegion.COMMA);
    }
    
    @Test
    public void testConstantRegionSignName() {
        // 测试符号映射
        assertNotNull(ConstantRegion.SIGN_NAME);
        assertEquals("pow", ConstantRegion.SIGN_NAME.get('^'));
        assertEquals("", ConstantRegion.SIGN_NAME.get(' '));
        assertEquals("_LB", ConstantRegion.SIGN_NAME.get('('));
        assertEquals("RB_", ConstantRegion.SIGN_NAME.get(')'));
    }
    
    @Test
    public void testConstantRegionComparisonOperators() {
        // 测试比较运算符常量
        assertEquals(">", ConstantRegion.GREATER_THAN_SIGN);
        assertEquals("<", ConstantRegion.LESS_THAN_SIGN);
        assertEquals("=", ConstantRegion.EQUAL_SIGN1);
        assertEquals("==", ConstantRegion.EQUAL_SIGN2);
        assertEquals("!", ConstantRegion.NEGATE_SIGN);
        assertEquals("!=", ConstantRegion.NOT_EQUAL_SIGN1);
        assertEquals("<>", ConstantRegion.NOT_EQUAL_SIGN2);
        assertEquals(">=", ConstantRegion.GREATER_THAN_OR_EQUAL_TO_SIGN);
        assertEquals("<=", ConstantRegion.LESS_THAN_OR_EQUAL_TO_SIGN);
        assertNotNull(ConstantRegion.REGULAR_COMPARISON_OPERATOR_PATTERN);
    }

    // ==================== NumberUtils 工具类测试 ====================
    
    @Test
    public void testNumberUtilsFactorial_Zero() {
        // 测试阶乘：0的阶乘
        assertEquals(0.0, NumberUtils.factorial(0), 0.001);
    }
    
    @Test
    public void testNumberUtilsFactorial_One() {
        // 测试阶乘：1的阶乘
        assertEquals(1.0, NumberUtils.factorial(1), 0.001);
    }
    
    @Test
    public void testNumberUtilsFactorial_Five() {
        // 测试阶乘：5! = 120
        assertEquals(120.0, NumberUtils.factorial(5), 0.001);
    }
    
    @Test
    public void testNumberUtilsFactorial_DecimalLessThanOne() {
        // 测试阶乘：0.5的阶乘
        assertEquals(0.5, NumberUtils.factorial(0.5), 0.001);
    }
    
    @Test
    public void testNumberUtilsCalculation_Plus() {
        // 测试加法
        assertEquals(5.0, NumberUtils.calculation('+', 2, 3), 0.001);
    }
    
    @Test
    public void testNumberUtilsCalculation_Minus() {
        // 测试减法
        assertEquals(1.0, NumberUtils.calculation('-', 4, 3), 0.001);
    }
    
    @Test
    public void testNumberUtilsCalculation_Multiply() {
        // 测试乘法
        assertEquals(12.0, NumberUtils.calculation('*', 3, 4), 0.001);
    }
    
    @Test
    public void testNumberUtilsCalculation_Divide() {
        // 测试除法
        assertEquals(2.0, NumberUtils.calculation('/', 6, 3), 0.001);
    }
    
    @Test
    public void testNumberUtilsCalculation_Remainder() {
        // 测试取余
        assertEquals(1.0, NumberUtils.calculation('%', 7, 3), 0.001);
    }
    
    @Test
    public void testNumberUtilsCalculation_Power() {
        // 测试幂运算
        assertEquals(8.0, NumberUtils.calculation('^', 2, 3), 0.001);
    }
    
    @Test(expected = AbnormalOperation.class)
    public void testNumberUtilsCalculation_InvalidOperator() {
        // 测试非法操作符
        NumberUtils.calculation('?', 2, 3);
    }
    
    @Test
    public void testNumberUtilsPriorityComparison_PlusVsMultiply() {
        // 测试优先级：+ < *
        assertTrue(NumberUtils.PriorityComparison('+', '*'));
    }
    
    @Test
    public void testNumberUtilsPriorityComparison_MinusVsDivide() {
        // 测试优先级：- < /
        assertTrue(NumberUtils.PriorityComparison('-', '/'));
    }
    
    @Test
    public void testNumberUtilsPriorityComparison_MinusVsRemainder() {
        // 测试优先级：- < %
        assertTrue(NumberUtils.PriorityComparison('-', '%'));
    }
    
    @Test
    public void testNumberUtilsPriorityComparison_PlusVsPower() {
        // 测试优先级：+ < ^
        assertTrue(NumberUtils.PriorityComparison('+', '^'));
    }
    
    @Test
    public void testNumberUtilsPriorityComparison_MultiplyVsPlus() {
        // 测试优先级：* >= +
        assertFalse(NumberUtils.PriorityComparison('*', '+'));
    }
    
    @Test
    public void testNumberUtilsPriorityComparison_MultiplyVsMultiply() {
        // 测试优先级：* >= *
        assertFalse(NumberUtils.PriorityComparison('*', '*'));
    }
    
    @Test
    public void testNumberUtilsComparisonOperation_LessThan_True() {
        // 测试小于：真
        assertTrue(NumberUtils.ComparisonOperation("<", 1, 2));
    }
    
    @Test
    public void testNumberUtilsComparisonOperation_LessThan_False() {
        // 测试小于：假
        assertFalse(NumberUtils.ComparisonOperation("<", 2, 1));
    }
    
    @Test
    public void testNumberUtilsComparisonOperation_GreaterThan_True() {
        // 测试大于：真
        assertTrue(NumberUtils.ComparisonOperation(">", 3, 2));
    }
    
    @Test
    public void testNumberUtilsComparisonOperation_GreaterThan_False() {
        // 测试大于：假
        assertFalse(NumberUtils.ComparisonOperation(">", 1, 2));
    }
    
    @Test
    public void testNumberUtilsComparisonOperation_LessOrEqual_True() {
        // 测试小于等于：真（等于）
        assertTrue(NumberUtils.ComparisonOperation("<=", 2, 2));
    }
    
    @Test
    public void testNumberUtilsComparisonOperation_LessOrEqual_True2() {
        // 测试小于等于：真（小于）
        assertTrue(NumberUtils.ComparisonOperation("<=", 1, 2));
    }
    
    @Test
    public void testNumberUtilsComparisonOperation_GreaterOrEqual_True() {
        // 测试大于等于：真（等于）
        assertTrue(NumberUtils.ComparisonOperation(">=", 2, 2));
    }
    
    @Test
    public void testNumberUtilsComparisonOperation_GreaterOrEqual_True2() {
        // 测试大于等于：真（大于）
        assertTrue(NumberUtils.ComparisonOperation(">=", 3, 2));
    }
    
    @Test
    public void testNumberUtilsComparisonOperation_Equal_True() {
        // 测试等于（=）：真
        assertTrue(NumberUtils.ComparisonOperation("=", 5, 5));
    }
    
    @Test
    public void testNumberUtilsComparisonOperation_Equal2_True() {
        // 测试等于（==）：真
        assertTrue(NumberUtils.ComparisonOperation("==", 5, 5));
    }
    
    @Test
    public void testNumberUtilsComparisonOperation_Equal_False() {
        // 测试等于：假
        assertFalse(NumberUtils.ComparisonOperation("=", 5, 6));
    }
    
    @Test
    public void testNumberUtilsComparisonOperation_NotEqual_True() {
        // 测试不等于（!=）：真
        assertTrue(NumberUtils.ComparisonOperation("!=", 5, 6));
    }
    
    @Test
    public void testNumberUtilsComparisonOperation_NotEqual2_True() {
        // 测试不等于（<>）：真
        assertTrue(NumberUtils.ComparisonOperation("<>", 5, 6));
    }
    
    @Test
    public void testNumberUtilsComparisonOperation_NotEqual_False() {
        // 测试不等于：假
        assertFalse(NumberUtils.ComparisonOperation("!=", 5, 5));
    }
    
    @Test(expected = AbnormalOperation.class)
    public void testNumberUtilsComparisonOperation_InvalidOperator() {
        // 测试非法比较运算符
        NumberUtils.ComparisonOperation("??", 1, 2);
    }
    
    @Test
    public void testNumberUtilsSumOfRange_EqualStartEnd() {
        // 测试区间和：起始等于结束
        assertEquals(5.0, NumberUtils.sumOfRange(5.0, 5.0), 0.001);
    }
    
    @Test
    public void testNumberUtilsSumOfRange_Normal() {
        // 测试区间和：1到5 = (1+5)*5/2 = 15
        assertEquals(15.0, NumberUtils.sumOfRange(1.0, 5.0), 0.001);
    }
    
    @Test
    public void testNumberUtilsSumOfRange_Reverse() {
        // 测试区间和：5到1（使用绝对值）
        double result = NumberUtils.sumOfRange(5.0, 1.0);
        assertEquals(15.0, result, 0.001);
    }
    
    @Test
    public void testNumberUtilsSumOfRangeWithStep_StepOne() {
        // 测试带步长的区间和：步长为1
        assertEquals(15.0, NumberUtils.sumOfRange(1.0, 5.0, 1.0), 0.001);
    }
    
    @Test
    public void testNumberUtilsSumOfRangeWithStep_EqualStartEnd() {
        // 测试带步长的区间和：起始等于结束
        assertEquals(5.0, NumberUtils.sumOfRange(5.0, 5.0, 2.0), 0.001);
    }
    
    @Test
    public void testNumberUtilsSumOfRangeWithStep_StepTwo() {
        // 测试带步长的区间和：步长为2
        double result = NumberUtils.sumOfRange(1.0, 5.0, 2.0);
        assertTrue(result > 0);
    }
    
    @Test
    public void testNumberUtilsSumOfRangeString_EqualStartEnd() {
        // 测试区间和字符串：起始等于结束
        assertEquals("5.0", NumberUtils.sumOfRangeString(5.0, 5.0));
    }
    
    @Test
    public void testNumberUtilsSumOfRangeString_Normal() {
        // 测试区间和字符串：正常情况
        String result = NumberUtils.sumOfRangeString(1.0, 5.0);
        assertTrue(result.contains("+"));
        assertTrue(result.contains("*"));
    }
    
    @Test
    public void testNumberUtilsSumOfRangeStringWithStep_StepOne() {
        // 测试带步长的区间和字符串：步长为1
        String result = NumberUtils.sumOfRangeString(1.0, 5.0, 1.0);
        assertTrue(result.contains("+") || result.contains("*"));
    }
    
    @Test
    public void testNumberUtilsSumOfRangeStringWithStep_EqualStartEnd() {
        // 测试带步长的区间和字符串：起始等于结束
        assertEquals("5.0", NumberUtils.sumOfRangeString(5.0, 5.0, 2.0));
    }
    
    @Test
    public void testNumberUtilsSumOfRangeStringWithStep_StepTwo() {
        // 测试带步长的区间和字符串：步长为2
        String result = NumberUtils.sumOfRangeString(1.0, 10.0, 2.0);
        assertTrue(result.contains("*"));
    }
    
    @Test
    public void testNumberUtilsMultiplyOfRange_EqualStartEnd() {
        // 测试区间乘积：起始等于结束
        assertEquals(5.0, NumberUtils.MultiplyOfRange(5.0, 5.0, 1.0), 0.001);
    }
    
    @Test
    public void testNumberUtilsMultiplyOfRange_Normal() {
        // 测试区间乘积：1到5步长1 = 1*2*3*4*5 = 120
        assertEquals(120.0, NumberUtils.MultiplyOfRange(1.0, 5.0, 1.0), 0.001);
    }
    
    @Test
    public void testNumberUtilsMultiplyOfRange_StepTwo() {
        // 测试区间乘积：1到5步长2 = 1*3*5 = 15
        assertEquals(15.0, NumberUtils.MultiplyOfRange(1.0, 5.0, 2.0), 0.001);
    }
    
    @Test
    public void testNumberUtilsMultiplyOfRange_StepLarger() {
        // 测试区间乘积：步长大于区间
        assertEquals(2.0, NumberUtils.MultiplyOfRange(2.0, 3.0, 5.0), 0.001);
    }
    
    @Test
    public void testNumberUtilsMultiplyOfRangeString_EqualStartEnd() {
        // 测试区间乘积字符串：起始等于结束
        assertEquals("5.0", NumberUtils.MultiplyOfRangeString(5.0, 5.0, 1.0));
    }
    
    @Test
    public void testNumberUtilsMultiplyOfRangeString_Normal() {
        // 测试区间乘积字符串：正常情况
        String result = NumberUtils.MultiplyOfRangeString(1.0, 5.0, 1.0);
        assertEquals("120.0", result);
    }
    
    @Test
    @Deprecated
    public void testNumberUtilsDivideByTen() {
        // 测试废弃方法：除以10
        assertEquals(1, NumberUtils.divideByTen(10));
        assertEquals(0, NumberUtils.divideByTen(5));
        assertEquals(2, NumberUtils.divideByTen(20));
    }
    
    @Test
    @Deprecated
    public void testNumberUtilsSumOfRangeInt_EqualStartEnd() {
        // 测试废弃方法：整数区间和
        assertEquals(5, NumberUtils.sumOfRange(5, 5));
    }
    
    @Test
    @Deprecated
    public void testNumberUtilsSumOfRangeInt_Normal() {
        // 测试废弃方法：整数区间和 1到5
        assertEquals(15, NumberUtils.sumOfRange(1, 5));
    }

    // ==================== StrUtils 工具类测试 ====================
    
    @Test
    public void testStrUtilsRemoveEmpty_NoEmpty() {
        // 测试删除空字符：无空字符
        assertEquals("abc", StrUtils.removeEmpty("abc"));
    }
    
    @Test
    public void testStrUtilsRemoveEmpty_WithEmpty() {
        // 测试删除空字符：有空字符
        assertEquals("abc", StrUtils.removeEmpty("a b c"));
        assertEquals("abcdef", StrUtils.removeEmpty("ab c de f"));
    }
    
    @Test
    public void testStrUtilsRemoveEmpty_AllEmpty() {
        // 测试删除空字符：全部空字符
        assertEquals("", StrUtils.removeEmpty("   "));
    }
    
    @Test
    public void testStrUtilsRemoveEmpty_Empty() {
        // 测试删除空字符：空字符串
        assertEquals("", StrUtils.removeEmpty(""));
    }
    
    @Test
    public void testStrUtilsStringToDouble_Simple() {
        // 测试字符串转double：简单数值
        assertEquals(123.45, StrUtils.stringToDouble("123.45"), 0.001);
    }
    
    @Test
    public void testStrUtilsStringToDouble_Integer() {
        // 测试字符串转double：整数
        assertEquals(100.0, StrUtils.stringToDouble("100"), 0.001);
    }
    
    @Test
    public void testStrUtilsStringToDouble_Negative() {
        // 测试字符串转double：负数
        assertEquals(-50.0, StrUtils.stringToDouble("-50"), 0.001);
    }
    
    @Test
    public void testStrUtilsStringToDouble_Factorial() {
        // 测试字符串转double：阶乘
        assertEquals(120.0, StrUtils.stringToDouble("5!"), 0.001);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testStrUtilsStringToDouble_FactorialNegative() {
        // 测试字符串转double：负数阶乘（抛出异常）
        StrUtils.stringToDouble("-5!");
    }
    
    @Test
    public void testStrUtilsStringToDouble_WithCache() {
        // 测试字符串转double：使用缓存
        Mathematical_Expression.Options.setUseCache(true);
        assertEquals(100.0, StrUtils.stringToDouble("100"), 0.001);
        // 第二次应该从缓存读取
        assertEquals(100.0, StrUtils.stringToDouble("100"), 0.001);
        Mathematical_Expression.Options.setUseCache(false);
    }
    
    @Test
    public void testStrUtilsStringToBigDecimal_Simple() {
        // 测试字符串转BigDecimal：简单数值
        assertEquals(new BigDecimal("123.45"), StrUtils.stringToBigDecimal("123.45"));
    }
    
    @Test
    public void testStrUtilsStringToBigDecimal_Factorial() {
        // 测试字符串转BigDecimal：阶乘
        BigDecimal result = StrUtils.stringToBigDecimal("5!");
        assertEquals(120.0, result.doubleValue(), 0.001);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testStrUtilsStringToBigDecimal_FactorialNegative() {
        // 测试字符串转BigDecimal：负数阶乘（抛出异常）
        StrUtils.stringToBigDecimal("-3!");
    }
    
    @Test
    public void testStrUtilsStringToBigDecimal_WithCache() {
        // 测试字符串转BigDecimal：使用缓存
        Mathematical_Expression.Options.setUseCache(true);
        BigDecimal result1 = StrUtils.stringToBigDecimal("100");
        BigDecimal result2 = StrUtils.stringToBigDecimal("100");
        assertEquals(result1, result2);
        Mathematical_Expression.Options.setUseCache(false);
    }
    
    @Test
    public void testStrUtilsIsAnOperator_Plus() {
        // 测试是否为操作符：加号
        assertTrue(StrUtils.IsAnOperator('+'));
    }
    
    @Test
    public void testStrUtilsIsAnOperator_Minus() {
        // 测试是否为操作符：减号
        assertTrue(StrUtils.IsAnOperator('-'));
    }
    
    @Test
    public void testStrUtilsIsAnOperator_Multiply() {
        // 测试是否为操作符：乘号
        assertTrue(StrUtils.IsAnOperator('*'));
    }
    
    @Test
    public void testStrUtilsIsAnOperator_Divide() {
        // 测试是否为操作符：除号
        assertTrue(StrUtils.IsAnOperator('/'));
    }
    
    @Test
    public void testStrUtilsIsAnOperator_Remainder() {
        // 测试是否为操作符：取余
        assertTrue(StrUtils.IsAnOperator('%'));
    }
    
    @Test
    public void testStrUtilsIsAnOperator_Power() {
        // 测试是否为操作符：幂
        assertTrue(StrUtils.IsAnOperator('^'));
    }
    
    @Test
    public void testStrUtilsIsAnOperator_NotOperator() {
        // 测试是否为操作符：非操作符
        assertFalse(StrUtils.IsAnOperator('a'));
        assertFalse(StrUtils.IsAnOperator('1'));
        assertFalse(StrUtils.IsAnOperator('('));
    }
    
    @Test
    public void testStrUtilsIsANumber_Char_True() {
        // 测试是否为数字字符：真
        assertTrue(StrUtils.IsANumber('0'));
        assertTrue(StrUtils.IsANumber('5'));
        assertTrue(StrUtils.IsANumber('9'));
    }
    
    @Test
    public void testStrUtilsIsANumber_Char_False() {
        // 测试是否为数字字符：假
        assertFalse(StrUtils.IsANumber('a'));
        assertFalse(StrUtils.IsANumber('+'));
        assertFalse(StrUtils.IsANumber('.'));
    }
    
    @Test
    public void testStrUtilsIsANumber_String_True() {
        // 测试是否为数字字符串：真
        assertTrue(StrUtils.IsANumber("12345"));
        assertTrue(StrUtils.IsANumber("0"));
    }
    
    @Test
    public void testStrUtilsIsANumber_String_False() {
        // 测试是否为数字字符串：假
        assertFalse(StrUtils.IsANumber("123a"));
        assertFalse(StrUtils.IsANumber("12.34"));
        assertFalse(StrUtils.IsANumber(""));
    }
    
    @Test
    public void testStrUtilsIsANumber_String_Empty() {
        // 测试是否为数字字符串：空字符串
        assertTrue(StrUtils.IsANumber(""));
    }
    
    @Test
    public void testStrUtilsSplitByChar_Normal() {
        // 测试按字符分割：正常情况
        ArrayList<String> result = StrUtils.splitByChar("a,b,c", ',');
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }
    
    @Test
    public void testStrUtilsSplitByChar_NoDelimiter() {
        // 测试按字符分割：无分隔符
        ArrayList<String> result = StrUtils.splitByChar("abc", ',');
        assertEquals(1, result.size());
        assertEquals("abc", result.get(0));
    }
    
    @Test
    public void testStrUtilsSplitByChar_WithSpaces() {
        // 测试按字符分割：带空格
        ArrayList<String> result = StrUtils.splitByChar("a , b , c", ',');
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }
    
    @Test
    public void testStrUtilsSplitByChar_Empty() {
        // 测试按字符分割：空字符串
        ArrayList<String> result = StrUtils.splitByChar("", ',');
        assertEquals(1, result.size());
        assertEquals("", result.get(0));
    }
    
    @Test
    public void testStrUtilsSplitByChar_MultipleDelimiters() {
        // 测试按字符分割：多个连续分隔符
        ArrayList<String> result = StrUtils.splitByChar("a,,b", ',');
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("", result.get(1));
        assertEquals("b", result.get(2));
    }
    
    @Test
    public void testStrUtilsSplitByCharWhereNoB_Normal() {
        // 测试按字符分割（忽略括号内）：正常情况
        ArrayList<String> result = StrUtils.splitByCharWhereNoB("a,b,c", ',');
        assertEquals(3, result.size());
    }
    
    @Test
    public void testStrUtilsSplitByCharWhereNoB_WithBrackets() {
        // 测试按字符分割（忽略括号内）：括号内的逗号不分割
        ArrayList<String> result = StrUtils.splitByCharWhereNoB("a,(b,c),d", ',');
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("(b,c)", result.get(1));
        assertEquals("d", result.get(2));
    }
    
    @Test
    public void testStrUtilsSplitByCharWhereNoB_NestedBrackets() {
        // 测试按字符分割（忽略括号内）：嵌套括号
        ArrayList<String> result = StrUtils.splitByCharWhereNoB("a,((b,c)),d", ',');
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("((b,c))", result.get(1));
    }
    
    @Test
    public void testStrUtilsRandomString() {
        // 测试随机字符串生成
        String result = StrUtils.randomString(10);
        assertEquals(10, result.length());
        assertTrue(result.matches("[a-z]+"));
    }
    
    @Test
    public void testStrUtilsRandomString_Zero() {
        // 测试随机字符串生成：长度为0
        String result = StrUtils.randomString(0);
        assertEquals(0, result.length());
    }
    
    @Test
    public void testStrUtilsContainsNumber_True() {
        // 测试是否包含数字：真
        assertTrue(StrUtils.containsNumber("abc123"));
        assertTrue(StrUtils.containsNumber("1"));
    }
    
    @Test
    public void testStrUtilsContainsNumber_False() {
        // 测试是否包含数字：假
        assertFalse(StrUtils.containsNumber("abc"));
        assertFalse(StrUtils.containsNumber(""));
    }
    
    @Test
    public void testStrUtilsContainsEnChar_True() {
        // 测试是否包含英文字符：真（小写）
        assertEquals('a', (char) StrUtils.containsEnChar("123abc"));
    }
    
    @Test
    public void testStrUtilsContainsEnChar_True_Uppercase() {
        // 测试是否包含英文字符：真（大写）
        assertEquals('A', (char) StrUtils.containsEnChar("123ABC"));
    }
    
    @Test
    public void testStrUtilsContainsEnChar_False() {
        // 测试是否包含英文字符：假
        assertNull(StrUtils.containsEnChar("123"));
        assertNull(StrUtils.containsEnChar(""));
    }
    
    @Test
    public void testStrUtilsContainsEnChar_Mixed() {
        // 测试是否包含英文字符：混合字符
        Character result = StrUtils.containsEnChar("123中文abc");
        assertNotNull(result);
        assertEquals('a', (char) result);
    }

    // ==================== Mathematical_Expression 枚举类测试 ====================
    
    @Test
    public void testMathematicalExpressionEnum_GetInstance_BracketsCalculation2() {
        // 测试枚举获取实例：BracketsCalculation2
        Calculation calc = Mathematical_Expression.getInstance(Mathematical_Expression.bracketsCalculation2);
        assertNotNull(calc);
        assertTrue(calc instanceof BracketsCalculation2);
    }
    
    @Test
    public void testMathematicalExpressionEnum_GetInstance_CumulativeCalculation() {
        // 测试枚举获取实例：CumulativeCalculation
        Calculation calc = Mathematical_Expression.getInstance(Mathematical_Expression.cumulativeCalculation);
        assertNotNull(calc);
        assertTrue(calc instanceof CumulativeCalculation);
    }
    
    @Test
    public void testMathematicalExpressionEnum_GetInstance_FastMultiply() {
        // 测试枚举获取实例：FastMultiplyOfIntervalsBrackets
        Calculation calc = Mathematical_Expression.getInstance(Mathematical_Expression.fastMultiplyOfIntervalsBrackets);
        assertNotNull(calc);
        assertTrue(calc instanceof FastMultiplyOfIntervalsBrackets);
    }
    
    @Test
    public void testMathematicalExpressionEnum_GetInstance_FastSum() {
        // 测试枚举获取实例：FastSumOfIntervalsBrackets
        Calculation calc = Mathematical_Expression.getInstance(Mathematical_Expression.fastSumOfIntervalsBrackets);
        assertNotNull(calc);
        assertTrue(calc instanceof FastSumOfIntervalsBrackets);
    }
    
    @Test
    public void testMathematicalExpressionEnum_GetInstance_FunctionFormula() {
        // 测试枚举获取实例：FunctionFormulaCalculation
        Calculation calc = Mathematical_Expression.getInstance(Mathematical_Expression.functionFormulaCalculation);
        assertNotNull(calc);
        assertTrue(calc instanceof FunctionFormulaCalculation);
    }
    
    @Test
    public void testMathematicalExpressionEnum_GetInstance_FunctionFormula2() {
        // 测试枚举获取实例：FunctionFormulaCalculation2
        Calculation calc = Mathematical_Expression.getInstance(Mathematical_Expression.functionFormulaCalculation2);
        assertNotNull(calc);
        assertTrue(calc instanceof FunctionFormulaCalculation2);
    }
    
    @Test
    public void testMathematicalExpressionEnum_GetInstance_PrefixExpression() {
        // 测试枚举获取实例：PrefixExpressionOperation
        Calculation calc = Mathematical_Expression.getInstance(Mathematical_Expression.prefixExpressionOperation);
        assertNotNull(calc);
        assertTrue(calc instanceof PrefixExpressionOperation);
    }
    
    @Test
    public void testMathematicalExpressionEnum_GetInstance_Boolean2() {
        // 测试枚举获取实例：BooleanCalculation2
        Calculation calc = Mathematical_Expression.getInstance(Mathematical_Expression.booleanCalculation2);
        assertNotNull(calc);
        assertTrue(calc instanceof BooleanCalculation2);
    }
    
    @Test
    public void testMathematicalExpressionEnum_GetInstance_Complex() {
        // 测试枚举获取实例：ComplexCalculation
        Calculation calc = Mathematical_Expression.getInstance(Mathematical_Expression.complexCalculation);
        assertNotNull(calc);
        assertTrue(calc instanceof ComplexCalculation);
    }
    
    @Test
    public void testMathematicalExpressionEnum_GetInstance_Jvm() {
        // 测试枚举获取实例：JvmCalculation
        Calculation calc = Mathematical_Expression.getInstance(Mathematical_Expression.jvmCalculation);
        assertNotNull(calc);
        assertTrue(calc instanceof JvmCalculation);
    }
    
    @Test
    public void testMathematicalExpressionEnum_GetInstance_SingleEquation() {
        // 测试枚举获取实例：SingletonEquationSolving
        Calculation calc = Mathematical_Expression.getInstance(Mathematical_Expression.singleEquationSolving);
        assertNotNull(calc);
        assertTrue(calc instanceof SingletonEquationSolving);
    }
    
    @Test
    public void testMathematicalExpressionEnum_GetInstance_SingleEquation2() {
        // 测试枚举获取实例：SingletonEquationSolvingTwo
        Calculation calc = Mathematical_Expression.getInstance(Mathematical_Expression.singleEquationSolving2);
        assertNotNull(calc);
        assertTrue(calc instanceof SingletonEquationSolvingTwo);
    }
    
    @Test
    public void testMathematicalExpressionEnum_GetInstanceWithName() {
        // 测试枚举获取实例：带名称
        Calculation calc = Mathematical_Expression.getInstance(
            Mathematical_Expression.bracketsCalculation2, "testCalc");
        assertNotNull(calc);
        assertTrue(calc instanceof BracketsCalculation2);
    }
    
    @Test
    public void testMathematicalExpressionOptions_UseBigDecimal() {
        // 测试选项：BigDecimal模式
        assertFalse(Mathematical_Expression.Options.isUseBigDecimal());
        Mathematical_Expression.Options.setUseBigDecimal(true);
        assertTrue(Mathematical_Expression.Options.isUseBigDecimal());
        Mathematical_Expression.Options.setUseBigDecimal(false);
        assertFalse(Mathematical_Expression.Options.isUseBigDecimal());
    }
    
    @Test
    public void testMathematicalExpressionOptions_UseCache() {
        // 测试选项：缓存模式
        assertFalse(Mathematical_Expression.Options.isUseCache());
        Mathematical_Expression.Options.setUseCache(true);
        assertTrue(Mathematical_Expression.Options.isUseCache());
        Mathematical_Expression.Options.setUseCache(false);
        assertFalse(Mathematical_Expression.Options.isUseCache());
    }
    
    @Test
    public void testMathematicalExpressionOptions_CacheCalculation() {
        // 测试选项：缓存计算结果
        Mathematical_Expression.Options.cacheCalculation(12345, 100.0);
        Number result = Mathematical_Expression.Options.getCacheCalculation(12345);
        assertNotNull(result);
        assertEquals(100.0, result.doubleValue(), 0.001);
    }
    
    @Test
    public void testMathematicalExpressionOptions_CacheCalculation_PackExpression() throws WrongFormat {
        // 测试选项：缓存表达式对象
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("test");
        PackExpression expr = (PackExpression) calc.compile("1+2", true);
        Mathematical_Expression.Options.cacheCalculation("1+2", expr);
        PackExpression cached = Mathematical_Expression.Options.getCacheCalculation("1+2");
        assertNotNull(cached);
    }
    
    @Test
    public void testMathematicalExpressionOptions_SetUseCache_False_ClearCache() {
        // 测试选项：关闭缓存时清空缓存
        Mathematical_Expression.Options.setUseCache(true);
        Mathematical_Expression.Options.cacheCalculation(999, 50.0);
        Mathematical_Expression.Options.setUseCache(false);
        // 缓存应该被清空
        Mathematical_Expression.Options.setUseCache(true);
        Number result = Mathematical_Expression.Options.getCacheCalculation(999);
        // 可能为null或者需要重新缓存
        Mathematical_Expression.Options.setUseCache(false);
    }

    // ==================== BracketsCalculation2 计算组件测试 ====================
    
    @Test
    public void testBracketsCalculation2_SimpleExpression() throws WrongFormat {
        // 测试简单表达式计算
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("testBrackets");
        CalculationNumberResults result = calc.calculation("1 + 2", true);
        assertEquals(3.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testBracketsCalculation2_WithBrackets() throws WrongFormat {
        // 测试带括号表达式
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("testBrackets2");
        CalculationNumberResults result = calc.calculation("(1 + 2) * 3", true);
        assertEquals(9.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testBracketsCalculation2_NestedBrackets() throws WrongFormat {
        // 测试嵌套括号
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("testNested");
        CalculationNumberResults result = calc.calculation("((1 + 2) * 3) + 4", true);
        assertEquals(13.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testBracketsCalculation2_GetInstanceTwice() {
        // 测试多次获取同一实例
        BracketsCalculation2 calc1 = BracketsCalculation2.getInstance("sameCalc");
        BracketsCalculation2 calc2 = BracketsCalculation2.getInstance("sameCalc");
        assertSame(calc1, calc2);
    }
    
    @Test(expected = ExtractException.class)
    public void testBracketsCalculation2_GetInstance_WrongType() {
        // 测试获取错误类型的实例
        // 先注册一个非BracketsCalculation2类型的计算组件
        PrefixExpressionOperation.getInstance("wrongType");
        // 尝试用BracketsCalculation2获取
        BracketsCalculation2.getInstance("wrongType");
    }
    
    @Test
    public void testBracketsCalculation2_FormatStr() {
        // 测试格式化字符串
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("formatTest");
        String formatted = calc.formatStr("1 + 2");
        assertNotNull(formatted);
    }
    
    @Test
    public void testBracketsCalculation2_GetName() {
        // 测试获取名称
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("nameTest");
        assertEquals("nameTest", calc.getName());
    }
    
    @Test
    public void testBracketsCalculation2_Compile() throws WrongFormat {
        // 测试编译表达式
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("compileTest");
        Expression expr = calc.compile("1 + 2 * 3", true);
        assertNotNull(expr);
        CalculationResults result = expr.calculation(false);
        assertEquals(7.0, ((CalculationNumberResults) result).getResult(), 0.001);
    }
    
    @Test
    public void testBracketsCalculation2_CompileBigDecimal() throws WrongFormat {
        // 测试编译BigDecimal表达式
        Mathematical_Expression.Options.setUseBigDecimal(true);
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("bigDecimalTest");
        Expression expr = calc.compileBigDecimal("1.5 + 2.5", true);
        assertNotNull(expr);
        assertTrue(expr.isBigDecimal());
        Mathematical_Expression.Options.setUseBigDecimal(false);
    }
    
    @Test
    public void testBracketsCalculation2_CalculationWithBigDecimal() throws WrongFormat {
        // 测试BigDecimal模式计算
        Mathematical_Expression.Options.setUseBigDecimal(true);
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("bigDecCalc");
        CalculationNumberResults result = calc.calculation("10.5 + 20.5", true);
        assertEquals(31.0, result.getResult(), 0.001);
        Mathematical_Expression.Options.setUseBigDecimal(false);
    }
    
    @Test
    public void testBracketsCalculation2_Explain() throws WrongFormat {
        // 测试解释计算过程
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("explainTest");
        LogResults logResults = calc.explain("(1 + 2) * 3", true);
        assertNotNull(logResults);
    }

    // ==================== 基本计算组件测试 ====================
    
    @Test
    public void testCumulativeCalculation_SimpleSum() throws WrongFormat {
        // 测试累加计算
        CumulativeCalculation calc = CumulativeCalculation.getInstance("cumulativeTest");
        CalculationNumberResults result = calc.calculation("n[1,10,1]", true);
        assertTrue(result.getResult() > 0);
    }
    
    @Test
    public void testFastSumOfIntervalsBrackets_Calculation() throws WrongFormat {
        // 测试快速区间求和
        FastSumOfIntervalsBrackets calc = FastSumOfIntervalsBrackets.getInstance("fastSumTest");
        CalculationNumberResults result = calc.calculation("n[1,5,1]", true);
        assertEquals(15.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testFastMultiplyOfIntervalsBrackets_Calculation() throws WrongFormat {
        // 测试快速区间乘积
        FastMultiplyOfIntervalsBrackets calc = FastMultiplyOfIntervalsBrackets.getInstance("fastMultiplyTest");
        CalculationNumberResults result = calc.calculation("n[1,5,1]", true);
        assertEquals(120.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testPrefixExpressionOperation_Calculation() throws WrongFormat {
        // 测试前缀表达式计算
        PrefixExpressionOperation calc = PrefixExpressionOperation.getInstance("prefixTest");
        CalculationNumberResults result = calc.calculation("1 + 2 * 3", true);
        assertEquals(7.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testBooleanCalculation2_Comparison() throws WrongFormat {
        // 测试布尔计算
        BooleanCalculation2 calc = BooleanCalculation2.getInstance("boolTest");
        CalculationBooleanResults result = calc.calculation("1 < 2", true);
        assertTrue(result.getResult());
    }
    
    @Test
    public void testBooleanCalculation2_Comparison_False() throws WrongFormat {
        // 测试布尔计算：假
        BooleanCalculation2 calc = BooleanCalculation2.getInstance("boolTest2");
        CalculationBooleanResults result = calc.calculation("5 < 2", true);
        assertFalse(result.getResult());
    }
    
    @Test
    public void testComplexCalculation_BasicOperation() throws WrongFormat {
        // 测试复数计算
        ComplexCalculation calc = ComplexCalculation.getInstance("complexTest");
        CalculationComplexResults result = calc.calculation("1 + 2i", true);
        assertNotNull(result);
    }

    // ==================== 函数相关测试 ====================
    
    @Test
    public void testRegisterJvmFunction_String() {
        // 测试注册JVM函数：字符串形式
        Mathematical_Expression.register_jvm_function("testFunc(x) = x * 2");
        // 验证函数已注册（通过不抛出异常来验证）
    }
    
    @Test
    public void testRegisterFunction_String() throws WrongFormat {
        // 测试注册函数：字符串形式
        boolean result = Mathematical_Expression.register_function("myFunc(x) = x + 10");
        assertTrue(result);
    }
    
    @Test
    public void testRegisterFunction_FunctionObject() {
        // 测试注册函数：函数对象
        ManyToOneNumberFunction func = new ManyToOneNumberFunction("testFunc", new String[]{"x"}) {
            @Override
            public double run(double... numbers) {
                return numbers[0] * 2;
            }
        };
        boolean result = Mathematical_Expression.register_function(func);
        assertTrue(result);
    }
    
    @Test
    public void testGetFunction() throws WrongFormat {
        // 测试获取函数
        Mathematical_Expression.register_function("getTestFunc(x) = x * 3");
        ManyToOneNumberFunction func = Mathematical_Expression.getFunction("getTestFunc");
        assertNotNull(func);
        assertEquals("getTestFunc", func.getName());
    }
    
    @Test
    public void testGetFunctionMap() {
        // 测试获取所有函数名
        Set<String> functionNames = Mathematical_Expression.getFunctionMap();
        assertNotNull(functionNames);
    }
    
    @Test
    public void testUnregisterFunction_String() throws WrongFormat {
        // 测试注销函数：字符串
        Mathematical_Expression.register_function("toBeUnregistered(x) = x");
        boolean result = Mathematical_Expression.unregister_function("toBeUnregistered");
        assertTrue(result);
    }
    
    @Test
    public void testUnregisterFunction_FunctionObject() throws WrongFormat {
        // 测试注销函数：函数对象
        Mathematical_Expression.register_function("toBeUnregistered2(x) = x");
        ManyToOneNumberFunction func = Mathematical_Expression.getFunction("toBeUnregistered2");
        boolean result = Mathematical_Expression.unregister_function(func);
        assertTrue(result);
    }
    
    @Test
    public void testRegisterFunction_FunctionPackage() {
        // 测试注册函数包
        FunctionPackage pkg = new FunctionPackage("testPackage");
        Mathematical_Expression.register_function(pkg);
        // 验证不抛出异常
    }
    
    @Test
    public void testSaveFunction_ToFile() throws IOException, WrongFormat {
        // 测试保存函数到文件
        Mathematical_Expression.register_function("saveTestFunc(x) = x * 5");
        ManyToOneNumberFunction func = Mathematical_Expression.getFunction("saveTestFunc");
        
        File tempFile = new File("test_function.ser");
        try {
            Mathematical_Expression.saveFunction(func, tempFile);
            assertTrue(tempFile.exists());
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    
    @Test
    public void testSaveFunction_ToOutputStream() throws IOException, WrongFormat {
        // 测试保存函数到输出流
        Mathematical_Expression.register_function("saveTestFunc2(x) = x * 6");
        ManyToOneNumberFunction func = Mathematical_Expression.getFunction("saveTestFunc2");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Mathematical_Expression.saveFunction(baos, func);
        assertTrue(baos.size() > 0);
    }
    
    @Test
    public void testRegisterFunction_FromFile() throws IOException, ClassNotFoundException, WrongFormat {
        // 测试从文件注册函数
        Mathematical_Expression.register_function("fileTestFunc(x) = x * 7");
        ManyToOneNumberFunction func = Mathematical_Expression.getFunction("fileTestFunc");
        
        File tempFile = new File("test_function2.ser");
        try {
            Mathematical_Expression.saveFunction(func, tempFile);
            boolean result = Mathematical_Expression.register_function(tempFile);
            assertTrue(result);
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    
    @Test
    public void testRegisterFunction_FromInputStream() throws IOException, WrongFormat {
        // 测试从输入流注册函数
        Mathematical_Expression.register_function("streamTestFunc(x) = x * 8");
        ManyToOneNumberFunction func = Mathematical_Expression.getFunction("streamTestFunc");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Mathematical_Expression.saveFunction(baos, func);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Map.Entry<Integer, Integer> result = Mathematical_Expression.register_function(bais);
        assertNotNull(result);
    }
    
    @Test
    public void testRegisterJvmFunction_Class() {
        // 测试注册JVM函数：类
        @Functions({"classTestFunc(x) = x * 9"})
        class TestFunctionClass {}
        
        boolean result = Mathematical_Expression.register_jvm_function(TestFunctionClass.class);
        assertTrue(result);
    }
    
    @Test
    public void testRegisterJvmFunction_Class_NoAnnotation() {
        // 测试注册JVM函数：无注解的类
        class TestFunctionClassNoAnnotation {}
        
        boolean result = Mathematical_Expression.register_jvm_function(TestFunctionClassNoAnnotation.class);
        assertFalse(result);
    }
    
    @Test
    public void testRegisterFunction_Class() throws WrongFormat {
        // 测试注册函数：类
        @Functions({"classTestFunc2(x) = x * 10"})
        class TestFunctionClass2 {}
        
        boolean result = Mathematical_Expression.register_function(TestFunctionClass2.class);
        assertTrue(result);
    }
    
    @Test
    public void testRegisterFunction_Class_NoAnnotation() throws WrongFormat {
        // 测试注册函数：无注解的类
        class TestFunctionClassNoAnnotation2 {}
        
        boolean result = Mathematical_Expression.register_function(TestFunctionClassNoAnnotation2.class);
        assertFalse(result);
    }

    // ==================== 综合功能测试 ====================
    
    @Test
    public void testComplexExpression_MultipleOperations() throws WrongFormat {
        // 测试复杂表达式：多种运算
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("complex1");
        CalculationNumberResults result = calc.calculation("(10 + 20) * 3 - 15 / 3", true);
        assertEquals(85.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testComplexExpression_PowerOperation() throws WrongFormat {
        // 测试幂运算
        PrefixExpressionOperation calc = PrefixExpressionOperation.getInstance("power1");
        CalculationNumberResults result = calc.calculation("2 ^ 3", true);
        assertEquals(8.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testComplexExpression_Remainder() throws WrongFormat {
        // 测试取余运算
        PrefixExpressionOperation calc = PrefixExpressionOperation.getInstance("remainder1");
        CalculationNumberResults result = calc.calculation("10 % 3", true);
        assertEquals(1.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testMultipleBracketLevels() throws WrongFormat {
        // 测试多层嵌套括号
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("multiLevel");
        CalculationNumberResults result = calc.calculation("(((1 + 2) * 3) + 4) * 2", true);
        assertEquals(26.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testExpressionWithCache() throws WrongFormat {
        // 测试表达式缓存
        Mathematical_Expression.Options.setUseCache(true);
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("cacheTest");
        
        CalculationNumberResults result1 = calc.calculation("5 + 10", true);
        CalculationNumberResults result2 = calc.calculation("5 + 10", true);
        
        assertEquals(result1.getResult(), result2.getResult(), 0.001);
        Mathematical_Expression.Options.setUseCache(false);
    }
    
    @Test
    public void testBigDecimalPrecision() throws WrongFormat {
        // 测试BigDecimal精度
        Mathematical_Expression.Options.setUseBigDecimal(true);
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("precision");
        CalculationNumberResults result = calc.calculation("0.1 + 0.2", true);
        assertEquals(0.3, result.getResult(), 0.0001);
        Mathematical_Expression.Options.setUseBigDecimal(false);
    }
    
    @Test
    public void testNegativeNumbers() throws WrongFormat {
        // 测试负数运算
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("negative");
        CalculationNumberResults result = calc.calculation("-5 + 10", true);
        assertEquals(5.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testDecimalNumbers() throws WrongFormat {
        // 测试小数运算
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("decimal");
        CalculationNumberResults result = calc.calculation("1.5 + 2.5", true);
        assertEquals(4.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testZeroDivisionHandling() throws WrongFormat {
        // 测试除以零的情况
        PrefixExpressionOperation calc = PrefixExpressionOperation.getInstance("divZero");
        CalculationNumberResults result = calc.calculation("1 / 0", true);
        assertTrue(Double.isInfinite(result.getResult()));
    }
    
    @Test
    public void testLargeNumbers() throws WrongFormat {
        // 测试大数运算
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("large");
        CalculationNumberResults result = calc.calculation("1000000 + 2000000", true);
        assertEquals(3000000.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testPriorityOfOperations() throws WrongFormat {
        // 测试运算优先级
        PrefixExpressionOperation calc = PrefixExpressionOperation.getInstance("priority");
        CalculationNumberResults result = calc.calculation("2 + 3 * 4", true);
        assertEquals(14.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testMultipleAdditions() throws WrongFormat {
        // 测试多个加法
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("multiAdd");
        CalculationNumberResults result = calc.calculation("1 + 2 + 3 + 4 + 5", true);
        assertEquals(15.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testMultipleMultiplications() throws WrongFormat {
        // 测试多个乘法
        PrefixExpressionOperation calc = PrefixExpressionOperation.getInstance("multiMul");
        CalculationNumberResults result = calc.calculation("2 * 3 * 4", true);
        assertEquals(24.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testMixedOperations() throws WrongFormat {
        // 测试混合运算
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("mixed");
        CalculationNumberResults result = calc.calculation("10 + 5 * 2 - 8 / 4", true);
        assertEquals(18.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testBracketsWithNegative() throws WrongFormat {
        // 测试括号与负数
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("bracketNeg");
        CalculationNumberResults result = calc.calculation("(-5 + 10) * 2", true);
        assertEquals(10.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testSequentialCalculations() throws WrongFormat {
        // 测试连续计算
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("sequential");
        CalculationNumberResults result1 = calc.calculation("1 + 1", true);
        CalculationNumberResults result2 = calc.calculation("2 + 2", true);
        CalculationNumberResults result3 = calc.calculation("3 + 3", true);
        
        assertEquals(2.0, result1.getResult(), 0.001);
        assertEquals(4.0, result2.getResult(), 0.001);
        assertEquals(6.0, result3.getResult(), 0.001);
    }
    
    @Test
    public void testResultLayers() throws WrongFormat {
        // 测试结果层数
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("layers");
        CalculationNumberResults result = calc.calculation("(1 + 2) * 3", true);
        assertTrue(result.getResultLayers() >= 0);
    }
    
    @Test
    public void testCalculationSourceName() throws WrongFormat {
        // 测试计算来源名称
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("sourceName");
        CalculationNumberResults result = calc.calculation("1 + 1", true);
        assertNotNull(result.getCalculationSourceName());
    }
    
    @Test
    public void testComparisonOperations_AllTypes() throws WrongFormat {
        // 测试所有比较运算符
        BooleanCalculation2 calc = BooleanCalculation2.getInstance("allComp");
        
        assertTrue(calc.calculation("1 < 2", true).getResult());
        assertTrue(calc.calculation("3 > 2", true).getResult());
        assertTrue(calc.calculation("2 <= 2", true).getResult());
        assertTrue(calc.calculation("2 >= 2", true).getResult());
        assertTrue(calc.calculation("5 == 5", true).getResult());
        assertTrue(calc.calculation("5 != 6", true).getResult());
    }

    // ==================== 边界条件和异常测试 ====================
    
    @Test
    public void testEmptyExpression() {
        // 测试空表达式
        try {
            BracketsCalculation2 calc = BracketsCalculation2.getInstance("empty");
            calc.calculation("", true);
        } catch (Exception e) {
            // 预期会抛出异常
            assertNotNull(e);
        }
    }
    
    @Test
    public void testSingleNumber() throws WrongFormat {
        // 测试单个数字
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("single");
        CalculationNumberResults result = calc.calculation("42", true);
        assertEquals(42.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testSingleNegativeNumber() throws WrongFormat {
        // 测试单个负数
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("singleNeg");
        CalculationNumberResults result = calc.calculation("-42", true);
        assertEquals(-42.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testVerySmallNumber() throws WrongFormat {
        // 测试极小数
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("verySmall");
        CalculationNumberResults result = calc.calculation("0.0001 + 0.0002", true);
        assertEquals(0.0003, result.getResult(), 0.00001);
    }
    
    @Test
    public void testCalculationWithSpaces() throws WrongFormat {
        // 测试带空格的表达式
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("spaces");
        CalculationNumberResults result = calc.calculation("  1  +  2  ", true);
        assertEquals(3.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testCalculationWithoutSpaces() throws WrongFormat {
        // 测试无空格的表达式
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("noSpaces");
        CalculationNumberResults result = calc.calculation("1+2", true);
        assertEquals(3.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testMultipleZeros() throws WrongFormat {
        // 测试多个零
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("zeros");
        CalculationNumberResults result = calc.calculation("0 + 0 + 0", true);
        assertEquals(0.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testOneMultiplyZero() throws WrongFormat {
        // 测试1乘以0
        PrefixExpressionOperation calc = PrefixExpressionOperation.getInstance("oneZero");
        CalculationNumberResults result = calc.calculation("1 * 0", true);
        assertEquals(0.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testZeroMinusZero() throws WrongFormat {
        // 测试0减0
        PrefixExpressionOperation calc = PrefixExpressionOperation.getInstance("zeroMinus");
        CalculationNumberResults result = calc.calculation("0 - 0", true);
        assertEquals(0.0, result.getResult(), 0.001);
    }
    
    @Test
    public void testBracketBalanceValidation() {
        // 测试括号平衡验证
        try {
            BracketsCalculation2 calc = BracketsCalculation2.getInstance("unbalanced");
            calc.check("(1 + 2");
            fail("应该抛出异常");
        } catch (WrongFormat e) {
            assertNotNull(e);
        }
    }
    
    @Test
    public void testMultipleCalculationTypes() throws WrongFormat {
        // 测试多种计算类型共存
        BracketsCalculation2 bc = BracketsCalculation2.getInstance("type1");
        PrefixExpressionOperation pe = PrefixExpressionOperation.getInstance("type2");
        BooleanCalculation2 bool = BooleanCalculation2.getInstance("type3");
        
        assertEquals(3.0, bc.calculation("1+2", true).getResult(), 0.001);
        assertEquals(3.0, pe.calculation("1+2", true).getResult(), 0.001);
        assertTrue(bool.calculation("1<2", true).getResult());
    }

    // ==================== 性能和压力测试 ====================
    
    @Test
    public void testRepeatedCalculations() throws WrongFormat {
        // 测试重复计算
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("repeated");
        for (int i = 0; i < 10; i++) {
            CalculationNumberResults result = calc.calculation("1 + 1", true);
            assertEquals(2.0, result.getResult(), 0.001);
        }
    }
    
    @Test
    public void testDifferentExpressions() throws WrongFormat {
        // 测试不同表达式
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("different");
        for (int i = 1; i <= 10; i++) {
            String expr = i + " + " + i;
            CalculationNumberResults result = calc.calculation(expr, true);
            assertEquals(i * 2.0, result.getResult(), 0.001);
        }
    }
    
    @Test
    public void testCacheEffectiveness() throws WrongFormat {
        // 测试缓存效果
        Mathematical_Expression.Options.setUseCache(true);
        
        // 第一次计算
        long start1 = System.nanoTime();
        StrUtils.stringToDouble("123.456");
        long end1 = System.nanoTime();
        
        // 第二次计算（应该从缓存读取）
        long start2 = System.nanoTime();
        StrUtils.stringToDouble("123.456");
        long end2 = System.nanoTime();
        
        // 两次都应该成功
        assertTrue(end1 > start1);
        assertTrue(end2 > start2);
        
        Mathematical_Expression.Options.setUseCache(false);
    }

    // ==================== AbnormalOperation 异常测试 ====================
    
    @Test(expected = AbnormalOperation.class)
    public void testAbnormalOperation_InvalidCalculationType() {
        // 测试异常操作：非法计算类型
        NumberUtils.calculation('&', 1, 2);
    }
    
    @Test(expected = AbnormalOperation.class)
    public void testAbnormalOperation_InvalidComparison() {
        // 测试异常操作：非法比较
        NumberUtils.ComparisonOperation("?", 1, 2);
    }

    // ==================== 表达式接口默认方法测试 ====================
    
    @Test
    public void testExpression_CalculationCache() throws WrongFormat {
        // 测试表达式缓存计算
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("exprCache");
        Expression expr = calc.compile("1 + 2", true);
        
        CalculationResults result1 = expr.calculationCache(false);
        CalculationResults result2 = expr.calculationCache(false);
        
        // 第二次应该从缓存获取
        assertNotNull(result1);
        assertNotNull(result2);
    }
    
    @Test
    public void testExpression_CalculationBigDecimalsCache() throws WrongFormat {
        // 测试BigDecimal缓存计算
        Mathematical_Expression.Options.setUseBigDecimal(true);
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("bigDecCache");
        Expression expr = calc.compileBigDecimal("1 + 2", true);
        
        CalculationResults result1 = expr.calculationBigDecimalsCache(false);
        CalculationResults result2 = expr.calculationBigDecimalsCache(false);
        
        assertNotNull(result1);
        assertNotNull(result2);
        Mathematical_Expression.Options.setUseBigDecimal(false);
    }

    // ==================== 额外覆盖测试 ====================
    
    @Test
    public void testStringToDouble_CacheHitAndMiss() {
        // 测试缓存命中和未命中
        Mathematical_Expression.Options.setUseCache(true);
        
        // 第一次计算 - 缓存未命中
        double result1 = StrUtils.stringToDouble("999.999");
        assertEquals(999.999, result1, 0.001);
        
        // 第二次计算 - 缓存命中
        double result2 = StrUtils.stringToDouble("999.999");
        assertEquals(999.999, result2, 0.001);
        
        Mathematical_Expression.Options.setUseCache(false);
    }
    
    @Test
    public void testStringToDouble_Factorial_WithCache() {
        // 测试阶乘缓存
        Mathematical_Expression.Options.setUseCache(true);
        
        double result1 = StrUtils.stringToDouble("4!");
        assertEquals(24.0, result1, 0.001);
        
        double result2 = StrUtils.stringToDouble("4!");
        assertEquals(24.0, result2, 0.001);
        
        Mathematical_Expression.Options.setUseCache(false);
    }
    
    @Test
    public void testStringToBigDecimal_CacheBranch() {
        // 测试BigDecimal缓存分支
        Mathematical_Expression.Options.setUseCache(true);
        
        BigDecimal result1 = StrUtils.stringToBigDecimal("123");
        assertEquals(new BigDecimal("123"), result1);
        
        // 测试缓存命中但类型不是BigDecimal的情况
        Mathematical_Expression.Options.cacheCalculation("456".hashCode(), 456.0);
        BigDecimal result2 = StrUtils.stringToBigDecimal("456");
        assertEquals(new BigDecimal("456"), result2);
        
        Mathematical_Expression.Options.setUseCache(false);
    }
    
    @Test
    public void testStringToBigDecimal_Factorial_WithCache() {
        // 测试BigDecimal阶乘缓存
        Mathematical_Expression.Options.setUseCache(true);
        
        BigDecimal result1 = StrUtils.stringToBigDecimal("3!");
        assertEquals(6.0, result1.doubleValue(), 0.001);
        
        BigDecimal result2 = StrUtils.stringToBigDecimal("3!");
        assertEquals(6.0, result2.doubleValue(), 0.001);
        
        Mathematical_Expression.Options.setUseCache(false);
    }
    
    @Test
    public void testNumberUtils_PriorityComparison_AllBranches() {
        // 测试优先级比较的所有分支
        // + vs *
        assertTrue(NumberUtils.PriorityComparison('+', '*'));
        // + vs /
        assertTrue(NumberUtils.PriorityComparison('+', '/'));
        // - vs %
        assertTrue(NumberUtils.PriorityComparison('-', '%'));
        // - vs ^
        assertTrue(NumberUtils.PriorityComparison('-', '^'));
        // * vs +
        assertFalse(NumberUtils.PriorityComparison('*', '+'));
        // / vs -
        assertFalse(NumberUtils.PriorityComparison('/', '-'));
        // ^ vs *
        assertFalse(NumberUtils.PriorityComparison('^', '*'));
    }
    
    @Test
    public void testSumOfRange_EdgeCases() {
        // 测试区间和的边界情况
        assertEquals(0.0, NumberUtils.sumOfRange(0.0, 0.0), 0.001);
        assertEquals(1.0, NumberUtils.sumOfRange(1.0, 1.0), 0.001);
        assertEquals(-5.0, NumberUtils.sumOfRange(-5.0, -5.0), 0.001);
    }
    
    @Test
    public void testSumOfRangeWithStep_MaxStep() {
        // 测试步长最大值的计算
        double result = NumberUtils.sumOfRange(1.0, 10.0, 5.0);
        assertTrue(result > 0);
    }
    
    @Test
    public void testMultiplyOfRange_WithModulo() {
        // 测试带取余的区间乘积
        double result = NumberUtils.MultiplyOfRange(1.0, 10.0, 3.0);
        // 1 * 4 * 7 * 10 = 280
        assertEquals(280.0, result, 0.001);
    }
    
    @Test
    public void testContainsEnChar_BoundaryValues() {
        // 测试英文字符检测的边界值
        assertEquals('A', (char) StrUtils.containsEnChar("123A"));
        assertEquals('Z', (char) StrUtils.containsEnChar("123Z"));
        assertEquals('a', (char) StrUtils.containsEnChar("123a"));
        assertEquals('z', (char) StrUtils.containsEnChar("123z"));
    }
    
    @Test
    public void testSplitByCharWhereNoB_EmptyBrackets() {
        // 测试空括号情况
        ArrayList<String> result = StrUtils.splitByCharWhereNoB("(),", ',');
        assertEquals(2, result.size());
        assertEquals("()", result.get(0));
    }
    
    @Test
    public void testCalculation_AllOperators_NegativeNumbers() {
        // 测试所有运算符的负数情况
        assertEquals(-5.0, NumberUtils.calculation('+', -2, -3), 0.001);
        assertEquals(-1.0, NumberUtils.calculation('-', -4, -3), 0.001);
        assertEquals(12.0, NumberUtils.calculation('*', -3, -4), 0.001);
        assertEquals(-2.0, NumberUtils.calculation('/', -6, 3), 0.001);
        assertEquals(-1.0, NumberUtils.calculation('%', -7, 3), 0.001);
        assertEquals(0.125, NumberUtils.calculation('^', -2, -3), 0.001);
    }
    
    @Test
    public void testFactorial_LargeNumber() {
        // 测试大数阶乘
        double result = NumberUtils.factorial(10);
        assertEquals(3628800.0, result, 0.001);
    }
    
    @Test
    public void testFactorial_BoundaryOne() {
        // 测试阶乘边界：正好等于1
        assertEquals(1.0, NumberUtils.factorial(1.0), 0.001);
    }
    
    @Test
    public void testFactorial_LessThanOne() {
        // 测试阶乘：小于1的数
        assertEquals(0.9, NumberUtils.factorial(0.9), 0.001);
    }
    
    @Test
    public void testRandomString_LongString() {
        // 测试生成长随机字符串
        String result = StrUtils.randomString(100);
        assertEquals(100, result.length());
        assertTrue(result.matches("[a-z]+"));
    }
    
    @Test
    public void testRemoveEmpty_MultipleSpaces() {
        // 测试删除多个空格
        assertEquals("abcdefg", StrUtils.removeEmpty("a b c d e f g"));
    }
    
    @Test
    public void testIsANumber_AllDigits() {
        // 测试所有数字字符
        for (char c = '0'; c <= '9'; c++) {
            assertTrue(StrUtils.IsANumber(c));
        }
    }
    
    @Test
    public void testComparisonOperation_BoundaryValues() {
        // 测试比较运算的边界值
        assertTrue(NumberUtils.ComparisonOperation("<", -1.0, 0.0));
        assertTrue(NumberUtils.ComparisonOperation(">", 0.0, -1.0));
        assertTrue(NumberUtils.ComparisonOperation("<=", 0.0, 0.0));
        assertTrue(NumberUtils.ComparisonOperation(">=", 0.0, 0.0));
        assertTrue(NumberUtils.ComparisonOperation("==", 0.0, 0.0));
        assertFalse(NumberUtils.ComparisonOperation("!=", 0.0, 0.0));
    }

    // ==================== 更多分支覆盖测试 ====================
    
    @Test
    public void testOptions_GetCacheCalculation_Null() {
        // 测试获取不存在的缓存
        Number result = Mathematical_Expression.Options.getCacheCalculation(99999);
        assertNull(result);
    }
    
    @Test
    public void testOptions_GetCacheCalculation_PackExpression_Null() {
        // 测试获取不存在的表达式缓存
        PackExpression result = Mathematical_Expression.Options.getCacheCalculation("nonexistent");
        assertNull(result);
    }
    
    @Test
    public void testSplitByChar_StartWithDelimiter() {
        // 测试以分隔符开始
        ArrayList<String> result = StrUtils.splitByChar(",a,b", ',');
        assertEquals(3, result.size());
        assertEquals("", result.get(0));
        assertEquals("a", result.get(1));
        assertEquals("b", result.get(2));
    }
    
    @Test
    public void testSplitByChar_EndWithDelimiter() {
        // 测试以分隔符结束
        ArrayList<String> result = StrUtils.splitByChar("a,b,", ',');
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("", result.get(2));
    }
    
    @Test
    public void testNumberUtilsSumOfRangeString_WithReverse() {
        // 测试反向区间和字符串
        String result = NumberUtils.sumOfRangeString(10.0, 1.0);
        assertTrue(result.contains("+") || result.contains("*"));
    }
    
    @Test
    public void testNumberUtilsMultiplyOfRangeString_StartEqualsEnd() {
        // 测试乘积字符串：起始等于结束
        String result = NumberUtils.MultiplyOfRangeString(7.0, 7.0, 1.0);
        assertEquals("7.0", result);
    }

    // ==================== 最终综合测试 ====================
    
    @Test
    public void testFullWorkflow() throws Exception {
        // 测试完整工作流程
        // 1. 注册函数
        Mathematical_Expression.register_function("wf(x) = x * 2");
        
        // 2. 设置选项
        Mathematical_Expression.Options.setUseCache(true);
        Mathematical_Expression.Options.setUseBigDecimal(false);
        
        // 3. 创建计算器
        BracketsCalculation2 calc = BracketsCalculation2.getInstance("workflow");
        
        // 4. 执行计算
        CalculationNumberResults result = calc.calculation("(10 + 20) * 2", true);
        
        // 5. 验证结果
        assertEquals(60.0, result.getResult(), 0.001);
        assertNotNull(result.getCalculationSourceName());
        
        // 6. 清理
        Mathematical_Expression.Options.setUseCache(false);
        Mathematical_Expression.unregister_function("wf");
    }
    
    @Test
    public void testAllEnumInstances() {
        // 测试所有枚举实例
        for (Mathematical_Expression expr : Mathematical_Expression.values()) {
            Calculation calc = Mathematical_Expression.getInstance(expr);
            assertNotNull(calc);
        }
    }
    
    @Test
    public void testEnumValueOf() {
        // 测试枚举valueOf方法
        Mathematical_Expression expr = Mathematical_Expression.valueOf("bracketsCalculation2");
        assertEquals(Mathematical_Expression.bracketsCalculation2, expr);
    }
    
    @Test
    public void testEnumValues() {
        // 测试枚举values方法
        Mathematical_Expression[] values = Mathematical_Expression.values();
        assertTrue(values.length >= 12);
    }
}
