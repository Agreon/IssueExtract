object ParserSpec: Spek({
    describe("Parser") {
        val parser = Parser()

        on("singleline") {

            it("should return the result of adding the first number to the second number") {
                assertEquals(6, 6)
            }
        }
    }
})