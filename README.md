# Project 1 - Get to the Point

###### Objectives
This assignment is designed to get you comfortable with a modern developer workflow that includes AI-assisted coding. You will use an AI tool (like Gemini, CoPilot, or TabNine) to help you develop a Java class, and you will learn to verify the AI's output using provided unit tests.

###### Concepts Covered
* Basic Java concepts
* Java class design
* AI code generation
* GitHub
* IntelliJ
* Git
* UML
* Unit Tests

###### Setup Details
* Review Java programming basics paying close attention to creating custom classes.
* Fork the project template from https://github.com/UltimateSandbox/project-1-initial.git
* Clone your new project from your own GitHub repository to your local machine.

###### Assignment Overview
Complete the following programming assignment to create a Point class that represents a point in the Cartesian plane. You will use AI to help generate the code for this class, and you will verify its correctness using unit tests.
* **Review the Class Design:** Carefully read the informal UML and the list of required methods for the Point class. This is your chance to understand the requirements before you start coding.
* **Prompt an AI:** Craft a clear and specific prompt for a code generation AI. Your goal is to get the AI to generate the complete Point class. Be sure to include all the requirements, such as the class name, required methods, and the rotation formula. For full points, your prompt should also address the "trick" in the rotate method (using the original x and y values).
* **Verify with Unit Tests:** Use the provided unit tests to check the AI's work. Run the tests with no modifications. If any tests fail, it means the AI's code is incorrect.
* **Debug the AI's Code:** If the tests fail, your task is to debug and fix the AI-generated code. This is a critical skill for working with AI in a professional setting. Do not simply ask the AI to try again; you must understand and correct the errors yourself.
* **Document Your Work:** Include the exact prompt text you used to generate the code in a multi-line comment just above the class header.

###### Methods to Implement
* A constructor that takes x and y coordinates as double arguments
* setX(double x), setY(double y) - standard setters
* getX(), getY() - standard getters
* setPoint(double x, double y) - set the coordinates of the point.
* shiftX(double n), shiftY(double n) - shift a point by a given amount along one of the axes.
* distance(Point p2) - finds the distance to point p2.
* rotate(double angle) - rotates the point by a specified (radian) angle around the origin
* Include a toString method as well. If you're not sure what the toString method does, look it up.  It's a very useful method to have in your classes.
* Any other methods you believe to be necessary.  Feel free to add any additional methods that you think would be useful for the Point class, but make sure they are relevant and enhance the functionality of the class.

###### Notes
* Your code should work with the unit tests provided.
* Use the tests with no code modifications to test your class for the correct results.
* Use the informal UML below as a reference.

![img.png](img.png)

###### Expected Output
Once you get your Point class compiling, when you run the unit tests, you should see all tests pass.

###### Helpful Hints
- The rotate method implementation has a trick to it that you need to consider when implementing. Remember, if you modify the x value, then try to use it in the y value calculation, you'll get the wrong answer.  (You need to temporarily store the new x value and use the original for the y calculation.)  Be sure the AI took this into consideration when auto generating the rotation method.
- Utilize the Math class for the algorithm implementation.  AI will probably do this automatically.
- Make good use of whitespace and comments to make your implementation as clean as possible.
- Use good, SOLID object-oriented programming principles (pun intended) in your implementation.

###### Deliverables
- Be sure you commit & push your code to GitHub.  If you don't push it, I won't be able to see it!
- Make sure your repo is **public**, or I won't be able to see it.
- Copy the URL for your repo (green button on your GitHub repos' page) and paste it into the Website URL field in Canvas and click Submit Assignment!
