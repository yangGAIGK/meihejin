from flask import Flask
from app1 import bp1
from app2 import bp2
from app3 import bp3
from app4 import init_bp4
from app5 import bp5

app = Flask(__name__)

# 注册 Blueprint
app.register_blueprint(bp1)
app.register_blueprint(bp2)
app.register_blueprint(bp3)
app.register_blueprint(init_bp4())
app.register_blueprint(bp5)

if __name__ == "__main__":
    app.run(debug=True)
