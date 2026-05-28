from os import getcwd, mkdir, listdir
from os.path import join, isfile
from sys import exit
from companydocumentreader import read_document as read_company
from persondocumentreader import read_document as read_person
from companydocumentcomparer import read_compare_document as compare_company

async def init_health_check():
    # Initialize the directory
    print("-- READER HEALTH CHECK --")
    uploads_folder_name_test = 'uploads_test'
    uploads_dir_test = join(getcwd(), uploads_folder_name_test)
    print("Test upload directory:", uploads_dir_test)
    files = [f for f in listdir(uploads_dir_test) if isfile(join(uploads_dir_test, f))]
    if(len(files) == 0):
        print("No files found... unsafe to continue.")
        exit()
    else:
        print("All found files in the folder:", files)

    # Testing company reader (file names must be "company" or "person")
    for file in files:
        print(f"--Testing: {file}")
        if 'company' in file:
            result = await read_company(join(uploads_dir_test, file))
            print(result)
            print('-'*80)
        elif 'person' in file:
            result = await read_person(join(uploads_dir_test, file))
            print(result)
            print('-'*80)
        elif 'compare' in file:
            result = await compare_company(join(uploads_dir_test, file))
            print(result)
            print('-'*80)

    print("-- Init health check passed --")