from http.server import BaseHTTPRequestHandler, HTTPServer
from sys import argv
import urllib.parse

class SimpleHTTPHandler(BaseHTTPRequestHandler):
    def _set_response(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def do_GET(self):
        parsed_path = urllib.parse.urlparse(self.path)
    
        formatted_data = str(parsed_path).replace("%20", "").replace("|", "\n").replace("&", "\n===\n").replace("', fragment='')", "")

        print(f"Got GET request,\nPath: {formatted_data}===\nHeaders:\n{str(self.headers)}\n")
        self._set_response()
        self.wfile.write("GET request for {}".format(parsed_path).encode('utf-8'))

def run_server(server_class=HTTPServer, handler_class=SimpleHTTPHandler, port=9878):
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    print('server is working:\n')
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    print('server stopped\n')

if __name__ == '__main__':
    if len(argv) == 2:
        run_server(port=int(argv[1]))
    else:
        run_server()
