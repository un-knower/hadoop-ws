object List { ...
def range(from: Int, end: Int): List[Int] =
if (from >= end) Nil else from :: range(from + 1, end)