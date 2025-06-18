#!/bin/bash

echo "Finding test files with JUnit assertions..."
echo "----------------------------------------"

# Find files with assertEquals
echo "Files with assertEquals:"
grep -r "assertEquals" --include="*.kt" ./src/test/kotlin | cut -d: -f1 | sort | uniq
echo ""

# Find files with assertNotEquals
echo "Files with assertNotEquals:"
grep -r "assertNotEquals" --include="*.kt" ./src/test/kotlin | cut -d: -f1 | sort | uniq
echo ""

# Find files with assertThrows from JUnit
echo "Files with JUnit assertThrows:"
grep -r "import org.junit.jupiter.api.assertThrows" --include="*.kt" ./src/test/kotlin | cut -d: -f1 | sort | uniq
echo ""

# Find files with assertTrue/assertFalse
echo "Files with assertTrue/assertFalse:"
grep -r "assert\(True\|False\)" --include="*.kt" ./src/test/kotlin | cut -d: -f1 | sort | uniq
echo ""

echo "Finding test files not using mockk..."
echo "----------------------------------------"
grep -r "import org.mockito" --include="*.kt" ./src/test/kotlin | cut -d: -f1 | sort | uniq
echo ""

echo "Done!"

