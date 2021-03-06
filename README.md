# AutoMock - Kotlin Version
A module project to automate your model for fastly tests before a release version.

## Libs Used
[KotlinPoet](https://github.com/square/kotlinpoet)

[AutoService](https://github.com/google/auto/tree/master/service)

## Module Application
**What? Why module? Why not is library?**
<p>We used AutoService generator and this library use javax processing that doesn't work in android library, just in modules.

**So, what I do?**
<p>You will need to import the modules in project
 
[Download Modules](https://drive.google.com/drive/folders/1iTOaUFonWmqgZ3rF3uBBr_W7quRH7sDN?usp=sharing)

**Hey wait, how i do this?**

[How to import module in project]()

## How it works
![](https://github.com/Wottrich/auto-mock-kotlin/blob/master/printscreens/Screen%20Shot%202019-03-21%20at%2015.34.02.png)

After building application, the processor start working, first it search the annotations and after start the progress to create files and save in application

## Simple to use
@MockModel and @MockField serve to control you generated file. 

![](https://github.com/Wottrich/auto-mock-kotlin/blob/master/printscreens/mainactivity.png)

After building

![](https://github.com/Wottrich/auto-mock-kotlin/blob/master/printscreens/mockmainactivity.png)
