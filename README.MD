# What is it about?

In protocol such as TLS, headers, body and even the URL query strings are encrypted. Yet the support of using Kyber as a post-quantum cryptography algorithm with TLS is still limited. This project showcase how you could emulate it with spring-boot server and okhttp clients using ServletFilter and interceptor, establishing a hybrid encryption scheme by sending aes encrypted payload (which key is exchanged using kyber) over the usuall https (the https is not configured in this example for simplicity, but can certainly be done).

Note that this project is experimental, and currently only has support for POST and GET requests and not yet tested for chunked request. In all fairness, an implementation directly on the http client level would be ideal and more scalable, but that will require dedication beyond what I currently able to provide.

## To Test It Out

To test it run the spring-boot server :
```
mvn spring-boot:run
```

Then run the client.

You should expect something that looks like this :

```
[1] Public key : MIIDNDANBgsrBgEEAYGwGgUGAQOCAyEAw5efF0UZlQpuE2GUH9TJZrUw+htLUnEoOwZco5gzO0vC/JuycWhse/BwLWR/VSMCqrpJRiNWBeJMabupkIxdPjrMsrej/FpeHReE1vRIOaV22LVOfEBuZlFn7AlqQgA6W7tVZmCtD3S9X4DP2IpLCziKGokTrGdeRHNMB3CUFocuIHYlAJxlpJGtfcHAp2FKaeQ3BHaNSyKrhDOdjHNZ+IUP8HKbNYFcx0GzTZFWX0dZxmgCylGkqgyaOBWqmChNwcxjVIJdspEvj+KMRoHM/nKitQYxDoGAWWvCqMrEoekot/YLXyW/4BgwuodNL+i6tCAMMkaupiSM3Ci0JmbLMpagyrR7lVt7TgZ9UzZcc8YL6sfID4yHciIPikW0j7ysbzqYGcdQ6vEQHAZ3wggNK3aSKjfE7LR/SrOxXsYVZWDMSHs5YMI12OCW12qg8ypCPvABdMklUeiHqEhgvbxaHVNfaEiMUGfBNuq5KJFhROAHXYUCzRE5QtCBlCcOXHLKHqsVTiIsO4eBvhe8CtaubumzPNmagfUg7mi4fZZoD1qLdmNgDVh+5XQ1NMCakMsPYwhIuHQ8ykpXpmF2tlfPyHwfZ6MmMKZpRAEl5qK55+I5C5YAg/U+XVpJNFAcc5e0jeQ9p0AGE0hqSroNErB8pOlwfqFJjnqzRuxSehwSczKqAYEP8rgCrLTAifMMe5FyTbR8IMyFFSCWvXLJ5kZ/LDBneoiOCDCAAGXIN4QElvictCkiHhtOWnE2UrxHp1WoBAEbilMOfrgOPcJ8Q7S5lLGwyFiLLakmIRA6EmsHEjQp0WLPsJjCjpuvcbVQaFTG+SyEXKVht1NCLcxtidWhdvy73KQrtcumw5olmdhgeVcnDWa5PSl72XFdn9RivuWadlAHFFRiHTSnfAw19JMHWnbGR8uFgPULMmU9aYMX8chmJENJDvbFVZx5OImbV/s8zKonQ9Egs+m8aZUSXrahEKYEMCoCyCBkD6lijEVPM1xfVykZIzmJqkqxioPHM9J0lspTV9rnFsbknhWFEA8Yre2Eipd4H0N5q9BSTh8otjg=

[2] Generated Aes key : H29+Ykzi8hF3IgXxkxFkcTTPuU1rS2EKnXCwOv5Ogo0=
[3] EncapsulatedKey : 5MbaJYr4KT9aokFqDlg7+4uXXuhS2hNSAL0wiSIhM58oE4BtOAQMGdJEw9HFvINh4hpFvxCkEZByeQc0NhFBAyEvAspYGBBI5uXJooug/1p+oL9l7qPEite7V8C28kkog+ubodccRgKrunQuocX6PITjtLI7o2olBWBfTqrd2H/DWtm+2XcrWxC1Z1SSw4xWE1MscNTXgqurlH1IRrViZ7s3pJ/9di+LJl0nxkQ7WiEccp43cKkVYCBH81oBkgVa0e2/YldK8DjZMOCYRv44aYxUOgmKzxcwFnH1fqi/aWVPJ8KblwhyrUiEuC+z1pN9p7kulHO6kQVfhH9fUleMp49DGYiWcjh3T5OI8/OXUqogvaub10w9swxDnQ82STGeoQNOa+6ZsnepY/R96nAAbu7cHlvXiola99c1MaKMpWVMbniu3d7uAX/8iHM7FfPgYH3k5xs+SbBAgMwX7uHoXDpBzyxi3vXULeLqdra25cl4qH1jlppP383IyMFsuy/zK/gmS0acp9taceJ51A6X4ttgNetQheSaF9ud0TgvbJ6tT99zmUfCIxOa9KeGIYkX9h+gdi39xOWSOLzi0/iERIWoIu1ll/zxAPzLQq7v/xXZPJQ1lP0p8nwx0mNoIUzG+1lXniS+SSl9hTHb98oMKCoGzgxfS7IQcYYzyLxnUQS2YmnCuGDCAUcxRpF8ivLgIn/A+6yzPJxBCjAoQvSFOP0M7o11rQ2tY8bSWV/PIDQfGJZ7P4HNxDFcioDZcmEETwGVGODGJpCTqo3j03EZ6Ucfuv+iYyKVA/Y5R6g8GlJyN2VSCRfeFU7WX5KKJAPYqark27P48ue3n6nlsXY3eex9CW4770W9lsoLXA8jLfWk1Jy9nDXPMQwwTIx16D2KrRT+pNbyHQ1nSJQu+Z7Bsx0F3EYhVL+vGg/hxoav3fJS/nwrI3TceGurfAfdehQYccDIM6MLskpX9Ei8nGhwKzzT/LI8VJ7HmGIwNRJZpPPpjmWIX8i9pOHklbSnyB93

[4]
Proceeding with request interceptor
--> POST http://localhost:8080/fkec/pre-handshake/handshake
Content-Type: application/json; charset=utf-8
Content-Length: 1050

{"encapsulated_secret":"5MbaJYr4KT9aokFqDlg7+4uXXuhS2hNSAL0wiSIhM58oE4BtOAQMGdJEw9HFvINh4hpFvxCkEZByeQc0NhFBAyEvAspYGBBI5uXJooug/1p+oL9l7qPEite7V8C28kkog+ubodccRgKrunQuocX6PITjtLI7o2olBWBfTqrd2H/DWtm+2XcrWxC1Z1SSw4xWE1MscNTXgqurlH1IRrViZ7s3pJ/9di+LJl0nxkQ7WiEccp43cKkVYCBH81oBkgVa0e2/YldK8DjZMOCYRv44aYxUOgmKzxcwFnH1fqi/aWVPJ8KblwhyrUiEuC+z1pN9p7kulHO6kQVfhH9fUleMp49DGYiWcjh3T5OI8/OXUqogvaub10w9swxDnQ82STGeoQNOa+6ZsnepY/R96nAAbu7cHlvXiola99c1MaKMpWVMbniu3d7uAX/8iHM7FfPgYH3k5xs+SbBAgMwX7uHoXDpBzyxi3vXULeLqdra25cl4qH1jlppP383IyMFsuy/zK/gmS0acp9taceJ51A6X4ttgNetQheSaF9ud0TgvbJ6tT99zmUfCIxOa9KeGIYkX9h+gdi39xOWSOLzi0/iERIWoIu1ll/zxAPzLQq7v/xXZPJQ1lP0p8nwx0mNoIUzG+1lXniS+SSl9hTHb98oMKCoGzgxfS7IQcYYzyLxnUQS2YmnCuGDCAUcxRpF8ivLgIn/A+6yzPJxBCjAoQvSFOP0M7o11rQ2tY8bSWV/PIDQfGJZ7P4HNxDFcioDZcmEETwGVGODGJpCTqo3j03EZ6Ucfuv+iYyKVA/Y5R6g8GlJyN2VSCRfeFU7WX5KKJAPYqark27P48ue3n6nlsXY3eex9CW4770W9lsoLXA8jLfWk1Jy9nDXPMQwwTIx16D2KrRT+pNbyHQ1nSJQu+Z7Bsx0F3EYhVL+vGg/hxoav3fJS/nwrI3TceGurfAfdehQYccDIM6MLskpX9Ei8nGhwKzzT/LI8VJ7HmGIwNRJZpPPpjmWIX8i9pOHklbSnyB93"}
--> END POST (1050-byte body)
<-- 200 http://localhost:8080/fkec/pre-handshake/handshake (20ms)
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sat, 04 May 2024 13:27:29 GMT
Keep-Alive: timeout=60
Connection: keep-alive

[5]
{"status":"00","session_info":"10ac45ed-99a1-4eb7-8dde-ed70bf00d589"}
<-- END HTTP (69-byte body)

[6]
Proceeding with request interceptor
Payload to encrypt : ~~~BODY~~~{"field1":"myField","field2":0,"field3":true}~~~PARAMS~~~param1=pararam&param2=2500~~~HEADERS~~~x-header1:MyHeader1||x-header2:MyHeader2
--> POST http://localhost:8080/fkec/post-handshake/transaction
Content-Type: text/plain; charset=utf-8
Content-Length: 232
x-session-info: 10ac45ed-99a1-4eb7-8dde-ed70bf00d589
x-orig-content-type: application/json; charset=utf-8

baBs+f+tZYZbzyIVVAjJh6uG2hKDsPv2mzp+k4XQS/bGYssiCqhl8uTEJStAn/rc8sPB5tGusk+kRHz56hjaXbZEtNC8MMrrW9C/B7furdjUKRb4NiJXTpriu2bVodsQM9ke9d706zj6+rSV5YZ6fWrGcvhOXW7lKhYI99ouPGoPW4Him/oZhhKsZZx2LybDMK5R2JyI1h9atbIZqChzNjsBPqMFiaaDtB8tJ/KR
--> END POST (232-byte body)

[7]
Received response body : 4SYAXt1m6v4PA3wGhVFlhOKk7acT7bhSFgqdotJdkGr3m/yvjrsxwe5At6aUQH7K5q0czvpBjzdMZ3M1kjh+LMGn8pO+/qkVPWzrMHQ4gBjsvW3vUZysqGT/p+tmv8Z/OJGuT45ZPt+z+NZkM3NboB4EnEIbb+gC9y4TdEjexzZO0ur/+3E=
<-- 200 http://localhost:8080/fkec/post-handshake/transaction (5ms)
Content-Type: text/plain;charset=ISO-8859-1
Content-Length: 180
Date: Sat, 04 May 2024 13:27:29 GMT
Keep-Alive: timeout=60
Connection: keep-alive
X-repshdr2: Resp Hdr2
X-repshdr1: Resp Hdr1

[8]
{"reps1":"resp1","resp2":0,"resp3":false}
<-- END HTTP (41-byte body)
TransactionResponse(reps1=resp1, resp2=0, resp3=false)
```

This is what happened :</br>
[1] A kyber public key is embedded into the client</br>
[2] The client generate an AES key</br>
[3] & [4] The aes key is encapsulated using kyber and sent to the server</br>
[5] The server respond with session info, indicating the key is received and the server will use that key for the following requests under that session</br>
[6] Subsequent reqeust will first be modified so that the body, parameters and headers are encrypted using the key echanged through kyber.</br>
[7] The response from the server is also encrypted and will be decrypted and re-constructed ont he client.</br>
[8] The rest of the program received the decrypted result and function as usual as if all that jazz doesn't happen.