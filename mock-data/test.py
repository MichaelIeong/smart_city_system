import requests

url = "https://api.aiproxy.io/v1/chat/completions"
headers = {
    "Authorization": "Bearer sk-cWRw4lQUFge6PtxqrwVvOnefwm8ZbuHGZAZcIPAYBz0pJa4L",
    "Content-Type": "application/json"
}
data = {
    "model": "gpt-4o",
    "messages": [{"role": "user", "content": "你是谁"}]
}

response = requests.post(url, json=data, headers=headers)
print(response.status_code, response.text)