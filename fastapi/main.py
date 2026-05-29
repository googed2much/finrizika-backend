import asyncio
import uvicorn
from sys import exit
from os import getcwd, mkdir, getenv
from os.path import join
from fastapi import FastAPI, UploadFile
from fastapi.responses import JSONResponse
from contextlib import asynccontextmanager
import uuid
from dotenv import load_dotenv

from healthchecker import init_health_check
from companydocumentreader import read_document as read_company
from persondocumentreader import read_document as read_person

# --------------------------------------------------------------------------------------------------
# Microservice initialization
# --------------------------------------------------------------------------------------------------

load_dotenv()

uploads_folder_name = 'uploads'
uploads_dir = join(getcwd(), uploads_folder_name)
try:
    mkdir(uploads_dir)
except FileExistsError:
    print(f"Directory '{uploads_dir}' already exists. Skipping")
except PermissionError:
    print(f"Permission denied: Unable to create '{uploads_dir}'.")
    exit()
except Exception as e:
    print(f"An error occurred: {e}")
    exit()

@asynccontextmanager
async def lifespan(app: FastAPI):
    try:
        await init_health_check()
        print("Health check initialized")
    except Exception as e:
        print(f"Health check failed: {e}")
    asyncio.create_task(worker()) 
    # startup finished
    yield

    # shutdown logic here if needed
    print("Shutting down...")

queue = asyncio.Queue()
results = {}
app = FastAPI(lifespan=lifespan)

async def worker():
    while True:
        job_id, kind, filepath = await queue.get()
        try:
            if kind == 'company':
                results[job_id] = await read_company(filepath)
            elif kind == 'person':
                results[job_id] = await read_person(filepath)
        except Exception as e:
            results[job_id] = {'error': str(e)}
        finally:
            queue.task_done()

@app.post("/api/read/company")
async def enqueue_company(file: UploadFile):
    if not file.filename:
        return JSONResponse(status_code=400, content={"error": "No file provided"})
    filepath = join(uploads_dir, file.filename)
    with open(filepath, "wb") as f:
        f.write(await file.read())
    job_id = str(uuid.uuid4())
    await queue.put((job_id, 'company', filepath))
    return {"job_id": job_id, "position": queue.qsize()}

@app.post("/api/read/person")
async def enqueue_person(file: UploadFile):
    if not file.filename:
        return JSONResponse(status_code=400, content={"error": "No file provided"})
    filepath = join(uploads_dir, file.filename)
    with open(filepath, "wb") as f:
        f.write(await file.read())
    job_id = str(uuid.uuid4())
    await queue.put((job_id, 'person', filepath))
    return {"job_id": job_id, "position": queue.qsize()}

@app.get("/api/result/{job_id}")
async def get_result(job_id: str):
    if job_id not in results:
        return JSONResponse(status_code=202, content={"status": "pending"})
    return results.pop(job_id)

# ---------------------------------------------------------------------------------------------------

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=int(getenv('APP_PORT')))

# ---------------------------------------------------------------------------------------------------