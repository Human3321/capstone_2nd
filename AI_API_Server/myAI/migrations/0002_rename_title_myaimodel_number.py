from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('myAI', '0001_initial'),
    ]

    operations = [
        migrations.RenameField(
            model_name='myaimodel',
            old_name='title',
            new_name='number',
        ),
    ]
